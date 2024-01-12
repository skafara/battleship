package battleship.client.controllers;

import java.io.IOException;

public class KeepAlive implements Runnable {

    private static final int INTERVAL_MS = 5_000;
    private static final Message KEEP_ALIVE_MESSAGE = new Message(Message.Type.KEEP_ALIVE);

    private final Communicator communicator;

    public KeepAlive(Communicator communicator) {
        this.communicator = communicator;
    }


    @Override
    public void run() {
        try {
            for (;;) {
                communicator.send(KEEP_ALIVE_MESSAGE);
                Thread.sleep(INTERVAL_MS);
            }
        }
        catch (IOException | InterruptedException e) {
            System.err.println("Stop KeepAlive Thread: " + e.getMessage());
        }
    }
}
