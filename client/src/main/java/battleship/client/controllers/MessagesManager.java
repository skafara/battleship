package battleship.client.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessagesManager implements Runnable {

    private final Communicator communicator;
    private final StateMachine stateMachine;
    private final Runnable onIOXeception;

    private CompletableFuture<Message> future;
    private Collection<Message.Type> awaitedMessageTypes;

    public MessagesManager(Communicator communicator, StateMachine stateMachine, Runnable onIOException) {
        this.communicator = communicator;
        this.stateMachine = stateMachine;
        this.onIOXeception = onIOException;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message message = communicator.receive();

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
            System.err.println("Stop MessagesManager Thread: " + e.getMessage());
            System.err.println("Start Reconnect Thread");
            new Thread(onIOXeception).start();
        }
    }

    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        future = new CompletableFuture<>();
        awaitedMessageTypes = new HashSet<>(List.of(type));
        return future;
    }

}
