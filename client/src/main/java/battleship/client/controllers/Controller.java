package battleship.client.controllers;

import battleship.client.controllers.exceptions.NotExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.BoardState;
import battleship.client.models.Model;
import battleship.client.views.Board;
import battleship.client.views.StageManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

                    new Thread(new KeepAlive(communicator)).start();

                    CompletableFuture<Message> welcomeFuture = expectMessage(Message.Type.WELCOME); // timeout (nebo jen odchytavat receive io exception?) + reconnect
                    new Thread(messagesManager).start();
                    welcomeFuture.get();

                    CompletableFuture<Message> ackFuture = expectMessage(Message.Type.ACK);
                    sendMessage(new Message(Message.Type.NICKNAME_SET, nickname));
                    ackFuture.get();

                    new Thread(stateMachine).start();

                    future.complete(null);
                }
                catch (IllegalArgumentException | IOException e) {
                    future.completeExceptionally(e);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
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
                Message response = responseFuture.get();

                if (response.getType() == Message.Type.ROOM_CREATED) {
                    model.applicationState.roomCodeProperty().set(response.getParameter(0));
                    future.complete(null);
                }
                else {
                    future.completeExceptionally(new ReachedLimitException(Integer.parseInt(response.getParameter(0))));
                }
            } catch (IOException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
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

        new Thread(() -> {
            CompletableFuture<Message> responseFuture = expectMessage(Message.Type.ACK, Message.Type.ROOM_FULL, Message.Type.ROOM_NOT_EXISTS);
            try {
                sendMessage(new Message(Message.Type.ROOM_JOIN, code));
                Message response = responseFuture.get();

                if (response.getType() == Message.Type.ACK) {
                    future.complete(null);
                }
                else if (response.getType() == Message.Type.ROOM_FULL) {
                    future.completeExceptionally(new ReachedLimitException(2));
                }
                else {
                    future.completeExceptionally(new NotExistsException());
                }

            } catch (IOException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
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
                responseFuture.get();

                model.applicationState.roomCodeProperty().set("");
                model.clientState.resetExceptNickname();
                model.opponentState.reset();
                future.complete(null);
            } catch (IOException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
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
                Message response = responseFuture.get();

                if (response.getType() == Message.Type.ACK) {
                    model.clientState.isBoardReadyProperty().set(true);
                    future.complete(null);
                }
                else {
                    future.completeExceptionally(new IllegalArgumentException());
                }
            } catch (IOException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
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
                Message response = responseFuture.get();

                if (response.getType() == Message.Type.TURN_RESULT) {
                    if (response.getParameter(1).equals("HIT")) {
                        model.opponentState.getBoard().setField(BoardState.Field.Hit, row, col);
                    }
                    else {
                        model.opponentState.getBoard().setField(BoardState.Field.Miss, row, col);
                    }
                    future.complete(null);
                }
                else if (response.getType() == Message.Type.TURN_ILLEGAL) {
                    future.completeExceptionally(new IllegalArgumentException());
                }
                else {
                    future.completeExceptionally(new IllegalStateException());
                }
            } catch (IOException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
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

}
