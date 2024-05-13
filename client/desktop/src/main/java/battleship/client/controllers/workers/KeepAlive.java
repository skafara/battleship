package battleship.client.controllers.workers;

import battleship.client.controllers.messages.Communicator;
import battleship.client.controllers.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Alive Keeper
 */
public class KeepAlive implements Runnable {

    private final Logger logger = LogManager.getLogger();

    private static final boolean IS_DEBUG = false;
    private static final int INTERVAL_MS = IS_DEBUG ? 30_000 : 5_000;
    private static final Message KEEP_ALIVE_MESSAGE = new Message(Message.Type.KEEP_ALIVE);

    private final Communicator communicator;

    /**
     * Constructs an Alive Keeper
     * @param communicator Messages Communicator
     */
    public KeepAlive(Communicator communicator) {
        this.communicator = communicator;
    }

    /**
     * Alive Keeper Loop
     */
    @Override
    public void run() {
        try {
            for (;;) {
                logger.trace("Sending Keep Alive Message");
                communicator.send(KEEP_ALIVE_MESSAGE);
                Thread.sleep(INTERVAL_MS);
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
        catch (InterruptedException e) {
            logger.trace("Interrupted");
        }
    }
}
