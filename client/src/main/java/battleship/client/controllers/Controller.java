package battleship.client.controllers;

import battleship.client.controllers.exceptions.ExistsException;
import battleship.client.controllers.exceptions.NotExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.controllers.messages.Communicator;
import battleship.client.controllers.messages.Message;
import battleship.client.controllers.workers.KeepAlive;
import battleship.client.controllers.workers.MessagesManager;
import battleship.client.controllers.workers.StateMachine;
import battleship.client.models.BoardState;
import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Application Controller
 */
public class Controller {

    private final Logger logger = LogManager.getLogger();

    private static final int RESPONSE_MSG_TIMEOUT_MS = 15_000;
    private static final int SOCKET_CONNECTION_TIMEOUT_MS = 10_000;
    private static final int RECONNECT_TIMEOUT_MS = 60_000;
    private static final int ROOM_CODE_LENGTH = 4;
    public static final int RECONNECT_ATTEMPT_SLEEP_MS = 5_000;

    private final Model model;

    private Socket socket;
    private Communicator communicator;
    private MessagesManager messagesManager;
    private StateMachine stateMachine;

    private Thread keepAliveThread;
    private Thread messagesManagerThread;
    private Thread stateMachineThread;

    private StageManager stageManager;

    /**
     * Constructs an application controller
     * @param model Application model
     */
    public Controller(Model model) {
        this.model = model;
    }

    /**
     * Sets a stage manager to use
     * @param stageManager Stage Manager
     */
    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    /**
     * Returns stage manager
     * @return Stage Manager
     */
    public StageManager getStageManager() {
        return stageManager;
    }

    /**
     * Connects to the server
     * Launches Messages Manager, Keep Alive, State Machine threads
     * Exceptions are forwarded through returned future
     * - IllegalArgument, Exists, ReachedLimit, Timeout, IO, Socket, Runtime
     * @param address Server address
     * @param port Server port
     * @param nickname Nickname
     * @return Future
     */
    public CompletableFuture<Void> connect(String address, String port, String nickname) {
        logger.trace("Connecting");

        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            int port_ = Integer.parseInt(port);
            new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port_), SOCKET_CONNECTION_TIMEOUT_MS);
                    communicator = new Communicator(socket);
                    stateMachine = new StateMachine(new StateMachineController(model, stageManager));
                    messagesManager = new MessagesManager(communicator, stateMachine, this::reconnect);

                    keepAliveThread = new Thread(new KeepAlive(communicator));
                    messagesManagerThread = new Thread(messagesManager);
                    stateMachineThread = new Thread(stateMachine);

                    CompletableFuture<Message> welcomeFuture = expectMessage(Message.Type.WELCOME, Message.Type.LIMIT_CLIENTS);
                    logger.debug("Start Messages Manager Thread");
                    messagesManagerThread.start();
                    Message welcomeMessage = awaitMessage(welcomeFuture);
                    if (welcomeMessage.getType() == Message.Type.LIMIT_CLIENTS) {
                        logger.info("Reached Clients Count Limit");
                        future.completeExceptionally(new ReachedLimitException(Integer.parseInt(welcomeMessage.getParameter(0))));
                    }
                    logger.info("Welcome");

                    CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.NICKNAME_EXISTS, Message.Type.REJOIN);
                    sendMessage(new Message(Message.Type.NICKNAME_SET, nickname));

                    Message message = awaitMessage(responseFuture);
                    if (message.getType() == Message.Type.ACK) {
                        logger.info("Nickname Set");
                        stageManager.setSceneLater(StageManager.Scene.Lobby);
                    }
                    else if (message.getType() == Message.Type.NICKNAME_EXISTS) {
                        logger.info("Nickname Exists");
                        future.completeExceptionally(new ExistsException());
                    }
                    else {
                        logger.info("Rejoin: " + message.getParameter(1));
                        model.applicationState.roomCodeProperty().set(message.getParameter(1));
                        stageManager.setSceneLater(StageManager.Scene.Room);
                    }

                    model.opponentState.isRespondingProperty().set(true);
                    model.clientState.isRespondingProperty().set(true);

                    logger.trace("Start Keep Alive Thread");
                    keepAliveThread.start();
                    logger.trace("Start State Machine Thread");
                    stateMachineThread.start();
                    future.complete(null);
                }
                catch (IOException | TimeoutException e) {
                    logger.error(e.getMessage());
                    future.completeExceptionally(e);
                }
                catch (RuntimeException e) {
                    handleRuntimeException();
                    future.completeExceptionally(e);
                }
            }).start();
        }
        catch (NumberFormatException e) {
            logger.trace("Invalid Number Format: " + e.getMessage());
            future.completeExceptionally(new IllegalArgumentException(e.getMessage()));
        }

        return future;
    }

    /**
     * Creates a room
     * Exceptions are forwarded through returned future
     * - ReachedLimit, IO, Timeout, Runtime
     * @return Future
     */
    public CompletableFuture<Void> createRoom() {
        logger.trace("Creating Room");

        CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ROOM_CREATED, Message.Type.LIMIT_ROOMS);
            try {
                sendMessage(new Message(Message.Type.ROOM_CREATE));
                Message response = awaitMessage(responseFuture);

                if (response.getType() == Message.Type.ROOM_CREATED) {
                    logger.info("Room Created: " + response.getParameter(0));
                    model.applicationState.roomCodeProperty().set(response.getParameter(0));
                    model.opponentState.isRespondingProperty().set(true);
                    future.complete(null);
                }
                else {
                    logger.info("Reached Rooms Count Limit");
                    future.completeExceptionally(new ReachedLimitException(Integer.parseInt(response.getParameter(0))));
                }
            }
            catch (IOException | TimeoutException e) {
                if (reconnect() && e instanceof TimeoutException) {
                    handleTimeout();
                }
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    /**
     * Joins a room
     * Exceptions are forwarded through returned future
     * - IllegalArgument, ReachedLimit, NotExists, IO, Timeout, Runtime
     * @param code Room code
     * @return Future
     */
    public CompletableFuture<Void> joinRoom(String code) {
        logger.trace("Joining Room: " + code);

        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            Integer.parseInt(code);
            if (code.length() != ROOM_CODE_LENGTH) {
                throw new IllegalArgumentException("Invalid Room Code Length");
            }
        }
        catch (IllegalArgumentException e) {
            logger.info(e.getMessage());
            future.completeExceptionally(e);
            return future;
        }

        model.opponentState.isRespondingProperty().set(true);
        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.ROOM_FULL, Message.Type.ROOM_NOT_EXISTS);
            try {
                sendMessage(new Message(Message.Type.ROOM_JOIN, code));
                Message response = awaitMessage(responseFuture);

                if (response.getType() == Message.Type.ACK) {
                    logger.info("Joined Room");
                    future.complete(null);
                } else if (response.getType() == Message.Type.ROOM_FULL) {
                    logger.info("Room Full");
                    future.completeExceptionally(new ReachedLimitException(2));
                } else {
                    logger.info("Room Not Exists");
                    future.completeExceptionally(new NotExistsException());
                }
            }
            catch (IOException | TimeoutException e) {
                if (reconnect() && e instanceof TimeoutException) {
                    handleTimeout();
                }
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    /**
     * Leaves a room
     * Exceptions are forwarded through returned future
     * - IO, Timeout, Runtime
     * @return Future
     */
    public CompletableFuture<Void> leaveRoom() {
        logger.trace("Leaving Room");

        CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK);
            try {
                sendMessage(new Message(Message.Type.ROOM_LEAVE));
                awaitMessage(responseFuture);
                logger.info("Leaved Room");

                model.applicationState.roomCodeProperty().set("");
                model.clientState.resetExceptNickname();
                model.opponentState.reset();
                future.complete(null);
            }
            catch (IOException | TimeoutException e) {
                if (reconnect() && e instanceof TimeoutException) {
                    handleTimeout();
                }
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    /**
     * Sets board ready
     * Exceptions are forwarded through returned future
     * - IllegalArgument, IO, Timeout, Runtime
     * @param boardState Board State
     * @return Future
     */
    public CompletableFuture<Void> boardReady(BoardState boardState) {
        logger.trace("Setting Board Ready");

        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!boardState.isValid()) {
            logger.info("Invalid Board");
            future.completeExceptionally(new IllegalArgumentException());
            return future;
        }

        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.BOARD_ILLEGAL);
            try {
                sendMessage(getBoardReadyMessage(boardState));
                Message response = awaitMessage(responseFuture);

                if (response.getType() == Message.Type.ACK) {
                    logger.info("Board Ready");
                    model.clientState.isBoardReadyProperty().set(true);
                    future.complete(null);
                }
                else {
                    logger.info("Invalid Board");
                    future.completeExceptionally(new IllegalArgumentException());
                }
            }
            catch (IOException | TimeoutException e) {
                if (reconnect() && e instanceof TimeoutException) {
                    handleTimeout();
                }
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    /**
     * Performs a turn
     * Exceptions are forwarded through returned future
     * - IllegalArgument, IllegalState, IO, Timeout, Runtime
     * @param row Row
     * @param col Col
     * @return Future
     */
    public CompletableFuture<Void> turn(int row, int col) {
        logger.info("Processing Turn");

        CompletableFuture<Void> future = new CompletableFuture<>();
        BoardState boardState = model.opponentState.getBoardState();
        if (boardState.isGuess(row, col) || boardState.isInvalidated(row, col)) {
            logger.info("Invalid Turn");
            future.completeExceptionally(new IllegalArgumentException());
            return future;
        }

        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.TURN_RESULT, Message.Type.TURN_ILLEGAL, Message.Type.TURN_NOT_YOU);
            try {
                sendMessage(new Message(Message.Type.TURN, BoardState.SerializeField(row, col)));
                Message response = awaitMessage(responseFuture);

                if (response.getType() == Message.Type.TURN_RESULT) {
                    if (response.getParameter(1).equals("HIT")) {
                        logger.info("Hit");
                        model.opponentState.getBoardState().setField(BoardState.Field.HIT, row, col);
                    }
                    else {
                        logger.info("Miss");
                        model.opponentState.getBoardState().setField(BoardState.Field.MISS, row, col);
                    }
                    future.complete(null);
                }
                else if (response.getType() == Message.Type.TURN_ILLEGAL) {
                    logger.info("Invalid Turn");
                    future.completeExceptionally(new IllegalArgumentException());
                }
                else {
                    logger.info("Not Your Turn");
                    future.completeExceptionally(new IllegalStateException());
                }
            }
            catch (IOException | TimeoutException e) {
                if (reconnect() && e instanceof TimeoutException) {
                    handleTimeout();
                }
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    /**
     * Sends a message
     * @param message Message
     * @throws IOException on IO error
     */
    public void sendMessage(Message message) throws IOException {
        logger.trace("Send Message: " + message.serialize());
        communicator.send(message);
    }

    /**
     * Sets a request for Messages Manager to expect a message
     * @param type Expected Message Type
     * @return Future
     */
    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        logger.trace("Expect Message: " + Arrays.toString(type));
        return messagesManager.expectMessage(type);
    }

    /**
     * Awaits an expected message
     * @param future Expected Message future
     * @return Message
     * @throws TimeoutException on timeout
     * @throws IOException on IO error
     */
    private Message awaitMessage(CompletableFuture<Message> future) throws TimeoutException, IOException {
        logger.trace("Awaiting Message");
        try {
            Message message = future.get(RESPONSE_MSG_TIMEOUT_MS, TimeUnit.MILLISECONDS); // throws TimeoutException
            logger.trace("Got Message");
            return message;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error Awaiting Message: " + e.getCause().getMessage());
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            else if (cause instanceof TimeoutException) {
                throw (TimeoutException) cause;
            }

            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a reconnect
     * Handles changes to model
     * Handles running threads interrupting and launching new ones
     * @return True if reconnect successful else false
     */
    private boolean reconnect() {
        logger.trace("Reconnecting");

        model.applicationState.setControlsDisable(true);
        model.clientState.isRespondingProperty().set(false);

        logger.debug("Interrupt Keep Alive Thread");
        keepAliveThread.interrupt();
        logger.debug("Interrupt State Machine Thread");
        stateMachineThread.interrupt();

        boolean isReconnected = false;
        long start = System.currentTimeMillis();
        for (long now = System.currentTimeMillis(); now < start + RECONNECT_TIMEOUT_MS; now = System.currentTimeMillis()) {
            try {
                logger.debug("Attempting Reconnect");
                socket = new Socket();
                socket.connect(new InetSocketAddress(model.applicationState.serverAddressProperty().get(), Integer.parseInt(model.applicationState.serverPortProperty().get())), SOCKET_CONNECTION_TIMEOUT_MS);
                communicator = new Communicator(socket);
                stateMachine = new StateMachine(new StateMachineController(model, stageManager));
                messagesManager = new MessagesManager(communicator, stateMachine, this::reconnect);

                keepAliveThread = new Thread(new KeepAlive(communicator));
                messagesManagerThread = new Thread(messagesManager);
                stateMachineThread = new Thread(stateMachine);

                CompletableFuture<Message> welcomeFuture = expectMessage(Message.Type.WELCOME, Message.Type.LIMIT_CLIENTS);
                logger.debug("Start Messages Manager Thread");
                messagesManagerThread.start();
                Message welcomeMessage = awaitMessage(welcomeFuture);
                if (welcomeMessage.getType() == Message.Type.LIMIT_CLIENTS) {
                    throw new ReachedLimitException(Integer.parseInt(welcomeMessage.getParameter(0)));
                }

                CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.NICKNAME_EXISTS, Message.Type.REJOIN);
                sendMessage(new Message(Message.Type.NICKNAME_SET, model.applicationState.nicknameProperty().get()));

                Message message = awaitMessage(responseFuture);
                if (message.getType() == Message.Type.ACK) {
                    stageManager.setSceneLater(StageManager.Scene.Lobby);
                } else if (message.getType() == Message.Type.NICKNAME_EXISTS) {
                    throw new ExistsException();
                } else {
                    model.applicationState.roomCodeProperty().set(message.getParameter(1));
                    stageManager.setSceneLater(StageManager.Scene.Room);
                }

                model.applicationState.setControlsDisable(false);
                model.clientState.isRespondingProperty().set(true);
                model.opponentState.isRespondingProperty().set(true);

                keepAliveThread.start();
                stateMachineThread.start();
                isReconnected = true;
                break;
            } catch (IOException | TimeoutException | ReachedLimitException | ExistsException e) {
                logger.debug("Attempt Failed: " + e.getMessage());
                logger.debug("Interrupting Messages Manager Thread");
                messagesManagerThread.interrupt();
                try {
                    Thread.sleep(RECONNECT_ATTEMPT_SLEEP_MS);
                } catch (InterruptedException ex) {
                    //
                }
            } catch (RuntimeException e) {
                handleRuntimeException();
                break;
            }
        }

        if (!isReconnected) {
            logger.error("Could Not Reconnect");
            logger.trace("Reset Model");
            model.reset();
            stageManager.setSceneLater(StageManager.Scene.Index);
            stageManager.showAlertLater(Alert.AlertType.ERROR, "Connection Error", "There have been problems connecting to the server. Please try again.");
            return false;
        }

        return true;
    }

    private static Message getBoardReadyMessage(BoardState boardState) {
        List<String> positions = new ArrayList<>();
        for (int row = 0; row < BoardState.SIZE; row++) {
            for (int col = 0; col < BoardState.SIZE; col++) {
                if (boardState.isShip(row, col)) {
                    positions.add(BoardState.SerializeField(row, col));
                }
            }
        }
        return new Message(Message.Type.BOARD_READY, positions.toArray());
    }

    private void handleTimeout() {
        logger.error("Request Timed Out");
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Request Timed Out", "Please try again.");
    }

    private void handleRuntimeException() {
        logger.error("Runtime Exception");
        logger.debug("Interrupting Workers");
        logger.debug("Resetting Model");

        messagesManagerThread.interrupt();
        keepAliveThread.interrupt();
        stateMachineThread.interrupt();
        model.reset();
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Runtime Exception", "Unexpected Error Occurred During Execution");
        stageManager.setSceneLater(StageManager.Scene.Index);
    }

}
