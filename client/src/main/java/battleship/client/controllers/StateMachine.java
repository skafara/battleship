package battleship.client.controllers;

import java.io.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class StateMachine implements Runnable {

    private final BlockingDeque<Message> messages = new LinkedBlockingDeque<>();

    public StateMachine() {
        //
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message msg = messages.take();
                System.out.println("Vyzvedavam zpravu " + msg.Serialize());
            }
        }
        catch (InterruptedException e) {
            //
        }
    }

    public void enqueueMessage(Message message) {
        messages.add(message);
    }

}
