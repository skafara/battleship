package battleship.client.controllers.messages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Messages Communicator
 */
public class Communicator {

    private final Logger logger = LogManager.getLogger();

    private static final char ESCAPE_CHARACTER = '\\';
    private static final char MESSAGE_DELIMITER = 0x0A;

    private final Object WRITE_ACCESS = new Object();

    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    /**
     * Creates a messages communicator on the socket
     * @param socket Socket
     * @throws IOException on IO error
     */
    public Communicator(Socket socket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
    }

    /**
     * Receives a message (blocks)
     * @return Message
     * @throws IOException on IO error
     */
    public Message receive() throws IOException {
        logger.trace("Receiving Message");
        StringBuilder stringBuilder = new StringBuilder();

        boolean escape = false;
        for (char c; ; ) {
            c = (char) bufferedReader.read();
            if (c == (char) -1) { // EOF
                throw new IOException();
            }

            if (!escape) {
                if (c == ESCAPE_CHARACTER) {
                    escape = true;
                    continue;
                }

                if (c == MESSAGE_DELIMITER) {
                    break;
                }
            }

            escape = false;
            stringBuilder.append(c);
        }

        Message message = Message.deserialize(stringBuilder.toString());
        logger.trace("Message Received: " + message.serialize());
        return message;
    }

    /**
     * Sends a message (does not block)
     * Escapes message delimiters (and escape characters)
     * @param message Message
     * @throws IOException on IO error
     */
    public void send(Message message) throws IOException {
        logger.trace("Sending Message: " + message.serialize());
        String text = message.serialize();

        synchronized (WRITE_ACCESS) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == ESCAPE_CHARACTER || c == MESSAGE_DELIMITER) {
                    bufferedWriter.write(ESCAPE_CHARACTER);
                }
                bufferedWriter.write(c);
            }
            bufferedWriter.write(MESSAGE_DELIMITER);
            bufferedWriter.flush();
        }
        logger.trace("Message Sent");
    }

}
