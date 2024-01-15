package battleship.client.controllers.workers;

import battleship.client.controllers.messages.Communicator;
import battleship.client.controllers.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Messages Manager
 * Receives messages, handles expected messages requests or passes the messages to the state machine
 */
public class MessagesManager implements Runnable {

    private final Logger logger = LogManager.getLogger();

    private static final boolean IS_DEBUG = false;
    private static final int MESSAGE_TIMEOUT_MS = IS_DEBUG ? 60_000 : 15_000;
    private final Object ACCESS_EXPECTED_MESSAGE = new Object();

    private final Communicator communicator;
    private final StateMachine stateMachine;
    private final Runnable onConnectionError;

    private CompletableFuture<Message> future;
    private Collection<Message.Type> awaitedMessageTypes;

    /**
     * Constructs a messages manager
     * @param communicator Messages Communicator
     * @param stateMachine State Machine
     * @param onConnectionError On Connection Error Handler
     */
    public MessagesManager(Communicator communicator, StateMachine stateMachine, Runnable onConnectionError) {
        this.communicator = communicator;
        this.stateMachine = stateMachine;
        this.onConnectionError = onConnectionError;
    }

    /**
     * Messages Manager Loop
     * In case of error, runs the handler in new thread
     */
    @Override
    public void run() {
        try {
            long lastActive = System.currentTimeMillis();
            for (;;) {
                logger.trace("Receiving Message");
                CompletableFuture<Message> futureMessage = new CompletableFuture<>();
                new Thread(() -> {
                    try {
                        futureMessage.complete(communicator.receive());
                    } catch (IOException e) {
                        futureMessage.completeExceptionally(e);
                    }
                }).start();

                Message message = futureMessage.get(lastActive - System.currentTimeMillis() + MESSAGE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                logger.trace("Message Received In Time: " + message.serialize());
                lastActive = System.currentTimeMillis();
                logger.info("Last Active: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastActive)));

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                if (message.getType() == Message.Type.KEEP_ALIVE) {
                    logger.info("Received Message: " + message);
                    continue;
                }

                synchronized (ACCESS_EXPECTED_MESSAGE) {
                    if (future != null && awaitedMessageTypes.contains(message.getType())) {
                        logger.debug("Completing Awaited Message Future: " + message.serialize());
                        future.complete(message);
                        future = null;
                        continue;
                    }
                }

                logger.trace("Enqueue Message: " + message.serialize());
                stateMachine.enqueueMessage(message);
            }
        }
        catch (TimeoutException e) {
            if (Thread.interrupted()) {
                return;
            }

            logger.error("Receive Message Timed Out");
            synchronized (ACCESS_EXPECTED_MESSAGE) {
                if (future != null) {
                    future.completeExceptionally(new TimeoutException());
                    return;
                }
            }

            new Thread(onConnectionError).start();
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                logger.error(e.getCause().getMessage());
                synchronized (ACCESS_EXPECTED_MESSAGE) {
                    if (future != null) {
                        future.completeExceptionally(e.getCause());
                        return;
                    }
                }

                new Thread(onConnectionError).start();
            }
        }
        catch (InterruptedException e) {
            //
        }
    }

    /**
     * Sets a request to expect a message
     * @param type Expected Message Type
     * @return Future
     */
    public CompletableFuture<Message> expectMessage(Message.Type... type) {
        logger.trace("Expect Message: " + Arrays.toString(type));
        future = new CompletableFuture<>();
        awaitedMessageTypes = new HashSet<>(List.of(type));
        return future;
    }

}
