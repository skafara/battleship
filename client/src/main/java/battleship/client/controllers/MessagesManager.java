package battleship.client.controllers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessagesManager implements Runnable {

    private final Communicator communicator;
    private final StateMachine stateMachine;

    private CompletableFuture<Message> future;
    private Message.Type[] awaitedMessageTypes;

    public MessagesManager(Communicator communicator, StateMachine stateMachine) {
        this.communicator = communicator;
        this.stateMachine = stateMachine;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message message = communicator.receive();

                if (future != null) {
                    if (List.of(awaitedMessageTypes).contains(message.getType())) {
                        future.complete(message);
                        future = null;
                    }
                } else {
                    stateMachine.enqueueMessage(message);
                }
            }
        } catch (IOException e) {
            //
        }
    }

    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        future = new CompletableFuture<>();
        awaitedMessageTypes = type;
        return future;
    }

}
