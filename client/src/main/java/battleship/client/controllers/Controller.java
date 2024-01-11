package battleship.client.controllers;

import battleship.client.controllers.exceptions.ExistsException;
import battleship.client.controllers.exceptions.NotExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.BoardState;
import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Controller {

    private static final int SOCKET_CONNECTION_TIMEOUT_MS = 10000;
    private static final int ROOM_CODE_LENGTH = 4;

    private final Model model;

    private Socket socket;
    private Communicator communicator;
    private MessagesManager messagesManager;

    private StageManager stageManager;

    private StateMachine stateMachine;

    public Controller(Model model) {
        this.model = model;
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public CompletableFuture<Void> connect(String address, String port, String nickname) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            int port_ = Integer.parseInt(port);
            new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port_), SOCKET_CONNECTION_TIMEOUT_MS);
                    communicator = new Communicator(socket);
                    stateMachine = new StateMachine(new StateMachineController(model, stageManager));
                    messagesManager = new MessagesManager(communicator, stateMachine);

                    CompletableFuture<Message> welcomeFuture = expectMessage(Message.Type.WELCOME, Message.Type.LIMIT_CLIENTS);
                    new Thread(messagesManager).start();
                    Message welcomeMessage = awaitMessage(welcomeFuture);
                    if (welcomeMessage.getType() == Message.Type.LIMIT_CLIENTS) {
                        future.completeExceptionally(new ReachedLimitException(Integer.parseInt(welcomeMessage.getParameter(0))));
                    }

                    CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.NICKNAME_EXISTS, Message.Type.REJOIN);
                    sendMessage(new Message(Message.Type.NICKNAME_SET, nickname));

                    Message message = awaitMessage(responseFuture);
                    if (message.getType() == Message.Type.ACK) {
                        stageManager.setSceneLater(StageManager.Scene.Lobby);
                    }
                    else if (message.getType() == Message.Type.NICKNAME_EXISTS) {
                        future.completeExceptionally(new ExistsException());
                    }
                    else {
                        model.applicationState.roomCodeProperty().set(message.getParameter(1));
                        stageManager.setSceneLater(StageManager.Scene.Room);
                    }

                    //new Thread(new KeepAlive(communicator)).start();

                    model.clientState.isRespondingProperty().set(true);
                    new Thread(stateMachine).start();

                    future.complete(null);
                }
                catch (IOException | RuntimeException e) {
                    System.err.println("socket");
                    future.completeExceptionally(e);
                }
                catch (TimeoutException e) {
                    System.err.println("timeout");
                    future.completeExceptionally(e);
                }
            }).start();
        }
        catch (NumberFormatException e) {
            future.completeExceptionally(new IllegalArgumentException(e.getMessage()));
        }

        return future;
    }

    public CompletableFuture<Void> createRoom() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ROOM_CREATED, Message.Type.LIMIT_ROOMS);
            try {
                sendMessage(new Message(Message.Type.ROOM_CREATE));
                Message response = awaitMessage(responseFuture);

                if (response.getType() == Message.Type.ROOM_CREATED) {
                    model.applicationState.roomCodeProperty().set(response.getParameter(0));
                    future.complete(null);
                }
                else {
                    future.completeExceptionally(new ReachedLimitException(Integer.parseInt(response.getParameter(0))));
                }
            }
            catch (IOException e) {
                // socket
                System.err.println("socket");
                future.completeExceptionally(e);
            }
            catch (TimeoutException e) {
                System.err.println("timeout");
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    public CompletableFuture<Void> joinRoom(String code) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            Integer.parseInt(code);
            if (code.length() != ROOM_CODE_LENGTH) {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e) {
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
                    future.complete(null);
                } else if (response.getType() == Message.Type.ROOM_FULL) {
                    future.completeExceptionally(new ReachedLimitException(2));
                } else {
                    future.completeExceptionally(new NotExistsException());
                }
            }
            catch (IOException e) {
                // socket
                System.err.println("socket");
                future.completeExceptionally(e);
            }
            catch (TimeoutException e) {
                System.err.println("timeout");
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    public CompletableFuture<Void> leaveRoom() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK);
            try {
                sendMessage(new Message(Message.Type.ROOM_LEAVE));
                awaitMessage(responseFuture);

                model.applicationState.roomCodeProperty().set("");
                model.clientState.resetExceptNickname();
                model.opponentState.reset();
                future.complete(null);
            }
            catch (IOException e) {
                // socket
                System.err.println("socket");
                future.completeExceptionally(e);
            }
            catch (TimeoutException e) {
                System.err.println("timeout");
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    public CompletableFuture<Void> boardReady(BoardState boardState) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!boardState.isValid()) {
            future.completeExceptionally(new IllegalArgumentException());
            return future;
        }

        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.BOARD_ILLEGAL);
            try {
                sendMessage(getBoardReadyMessage(boardState));
                Message response = awaitMessage(responseFuture);

                if (response.getType() == Message.Type.ACK) {
                    model.clientState.isBoardReadyProperty().set(true);
                    future.complete(null);
                }
                else {
                    future.completeExceptionally(new IllegalArgumentException());
                }
            }
            catch (IOException e) {
                // socket
                System.err.println("socket");
                future.completeExceptionally(e);
            }
            catch (TimeoutException e) {
                System.err.println("timeout");
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    public CompletableFuture<Void> turn(int row, int col) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        BoardState boardState = model.opponentState.getBoard();
        if (boardState.isGuess(row, col)) {
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
                        model.opponentState.getBoard().setField(BoardState.Field.HIT, row, col);
                    }
                    else {
                        model.opponentState.getBoard().setField(BoardState.Field.MISS, row, col);
                    }
                    future.complete(null);
                }
                else if (response.getType() == Message.Type.TURN_ILLEGAL) {
                    future.completeExceptionally(new IllegalArgumentException());
                }
                else {
                    future.completeExceptionally(new IllegalStateException());
                }
            }
            catch (IOException e) {
                // socket
                System.err.println("socket");
                future.completeExceptionally(e);
            }
            catch (TimeoutException e) {
                System.err.println("timeout");
                future.completeExceptionally(e);
            }
            catch (RuntimeException e) {
                handleRuntimeException();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    public void sendMessage(Message message) throws IOException {
        communicator.send(message);
    }

    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        return messagesManager.expectMessage(type);
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

    private Message awaitMessage(CompletableFuture<Message> future) throws TimeoutException, IOException {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof IOException) {
                throw new IOException();
            }
            throw new RuntimeException(e);
        }
    }

    private void handleRuntimeException() {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Runtime Exception", "Unexpected Error During Execution");
        stageManager.setSceneLater(StageManager.Scene.Index);
        model.reset();
    }

}
