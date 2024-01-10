package battleship.client.controllers;

import java.io.*;
import java.net.Socket;

public class Communicator {

    public final Object READ_ACCESS = new Object();

    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    public Communicator(Socket socket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
    }

    public Message receive() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        boolean escape = false;
        for (char c; ; ) {
            c = (char) bufferedReader.read();
            if (c == -1) { // EOF
                throw new IOException();
            }

            if (!escape) {
                if (c == '\\') {
                    escape = true;
                    continue;
                }

                if (c == '\n') {
                    break;
                }
            }

            escape = false;
            stringBuilder.append(c);
        }

        return Message.Deserialize(stringBuilder.toString());
    }

    public void send(Message message) throws IOException {
        String text = message.Serialize();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' || c == '\n') {
                bufferedWriter.write('\\');
            }
            bufferedWriter.write(c);
        }
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

}
