package battleship.client.controllers;

import java.io.IOException;

public class KeepAlive implements Runnable {

    private static final int INTERVAL_MS = 1000;

    private final Communicator communicator;

    public KeepAlive(Communicator communicator) {
        this.communicator = communicator;
    }


    @Override
    public void run() {
        try {
            for (;;) {
                communicator.send(new Message(Message.Type.KEEP_ALIVE));

                try {
                    Thread.sleep(INTERVAL_MS);
                }
                catch (InterruptedException e) {
                    //
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
