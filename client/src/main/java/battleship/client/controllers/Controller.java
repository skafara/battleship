package battleship.client.controllers;

import battleship.client.controllers.exceptions.NotExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.Model;
import battleship.client.views.StageManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
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
                    stateMachine = new StateMachine();
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
                sendMessage(new Message(Message.Type.ROOM_JOIN));
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

            future.complete(null);
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
                model.opponentState.reset();
                future.complete(null);
            } catch (IOException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            future.complete(null);
        }).start();

        return future;
    }

    public void turn(int row, int col) {

    }

    public void sendMessage(Message message) throws IOException {
        communicator.send(message);
    }

    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        return messagesManager.expectMessage(type);
    }

}
