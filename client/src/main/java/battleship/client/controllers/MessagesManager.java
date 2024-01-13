package battleship.client.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessagesManager implements Runnable {

    private final Communicator communicator;
    private final StateMachine stateMachine;
    private final Runnable onIOException;

    private CompletableFuture<Message> future;
    private Collection<Message.Type> awaitedMessageTypes;

    public MessagesManager(Communicator communicator, StateMachine stateMachine, Runnable onIOException) {
        this.communicator = communicator;
        this.stateMachine = stateMachine;
        this.onIOException = onIOException;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message message = communicator.receive();

                if (Thread.interrupted()) {
                    return;
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
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + " " + e.getMessage());
            if (Thread.interrupted()) {
                return;
            }

            new Thread(onIOException).start();
        }
    }

    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        future = new CompletableFuture<>();
        awaitedMessageTypes = new HashSet<>(List.of(type));
        return future;
    }

}
