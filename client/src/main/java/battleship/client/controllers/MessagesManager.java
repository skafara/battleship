package battleship.client.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessagesManager implements Runnable {

    private final Communicator communicator;
    private final StateMachine stateMachine;
    private final Runnable onConnectionError;
    private long lastActive;

    private CompletableFuture<Message> future;
    private Collection<Message.Type> awaitedMessageTypes;

    public MessagesManager(Communicator communicator, StateMachine stateMachine, Runnable onConnectionError) {
        this.communicator = communicator;
        this.stateMachine = stateMachine;
        this.onConnectionError = onConnectionError;
    }

    @Override
    public void run() {
        try {
            lastActive = System.currentTimeMillis();
            for (;;) {
                CompletableFuture<Message> futureMessage = new CompletableFuture<>();
                new Thread(() -> {
                    try {
                        futureMessage.complete(communicator.receive());
                    } catch (IOException e) {
                        futureMessage.completeExceptionally(e);
                    }
                }).start();

                Message message = futureMessage.get(lastActive - System.currentTimeMillis() + 15_000, TimeUnit.MILLISECONDS);
                lastActive = System.currentTimeMillis();

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                if (message.getType() == Message.Type.KEEP_ALIVE) {
                    continue;
                }

                if (future != null) {
                    if (awaitedMessageTypes.contains(message.getType())) {
                        future.complete(message);
                        future = null;
                    }
                } else {
                    stateMachine.enqueueMessage(message);
                }
            }
        }
        catch (TimeoutException e) {
            if (Thread.interrupted()) {
                return;
            }

            if (future != null) {
                future.completeExceptionally(new TimeoutException());
            }
            else {
                new Thread(onConnectionError).start();
            }
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                if (future != null) {
                    future.completeExceptionally(e.getCause());
                }
                else {
                    new Thread(onConnectionError).start();
                }
            }
        }
        catch (InterruptedException e) {
            //
        }
    }

    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        future = new CompletableFuture<>();
        awaitedMessageTypes = new HashSet<>(List.of(type));
        return future;
    }

}
