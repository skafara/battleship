package battleship.client.controllers.messages;

import battleship.client.models.BoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Message
 */
public class Message {

    /**
     * Message Type
     */
    public enum Type {
        WELCOME,
        CONN_TERM,
        KEEP_ALIVE,
        ACK,
        LIMIT_CLIENTS,
        NICKNAME_SET,
        NICKNAME_EXISTS,
        ROOM_CREATE,
        ROOM_CREATED,
        LIMIT_ROOMS,
        ROOM_JOIN,
        ROOM_FULL,
        ROOM_NOT_EXISTS,
        ROOM_LEAVE,
        BOARD_READY,
        BOARD_ILLEGAL,
        OPPONENT_NICKNAME_SET,
        OPPONENT_BOARD_READY,
        OPPONENT_ROOM_LEAVE,
        GAME_BEGIN,
        TURN_SET,
        OPPONENT_NO_RESPONSE,
        TURN,
        TURN_RESULT,
        TURN_ILLEGAL,
        TURN_NOT_YOU,
        OPPONENT_TURN,
        GAME_END,
        REJOIN,
        OPPONENT_REJOIN,
        BOARD_STATE,
        INVALIDATE_FIELD
    }

    /**
     * Incoming Messages Parameters Count
     */
    private static final Map<Type, Integer> PARAMETERS_COUNTS = Map.ofEntries(
            Map.entry(Type.WELCOME, 4),
            Map.entry(Type.KEEP_ALIVE, 0),
            Map.entry(Type.CONN_TERM, 0),
            Map.entry(Type.LIMIT_CLIENTS, 1),
            Map.entry(Type.ACK, 0),
            Map.entry(Type.NICKNAME_EXISTS, 0),
            Map.entry(Type.ROOM_CREATED, 1),
            Map.entry(Type.LIMIT_ROOMS, 1),
            Map.entry(Type.ROOM_FULL, 0),
            Map.entry(Type.ROOM_NOT_EXISTS, 0),
            Map.entry(Type.BOARD_ILLEGAL, 0),
            Map.entry(Type.OPPONENT_NICKNAME_SET, 1),
            Map.entry(Type.OPPONENT_BOARD_READY, 0),
            Map.entry(Type.OPPONENT_ROOM_LEAVE, 0),
            Map.entry(Type.GAME_BEGIN, 1),
            Map.entry(Type.TURN_SET, 1),
            Map.entry(Type.OPPONENT_NO_RESPONSE, 1),
            Map.entry(Type.TURN_RESULT, 2),
            Map.entry(Type.TURN_ILLEGAL, 0),
            Map.entry(Type.TURN_NOT_YOU, 0),
            Map.entry(Type.OPPONENT_TURN, 2),
            Map.entry(Type.GAME_END, 1),
            Map.entry(Type.REJOIN, 2),
            Map.entry(Type.OPPONENT_REJOIN, 0),
            Map.entry(Type.BOARD_STATE, 1 + BoardState.SIZE * BoardState.SIZE),
            Map.entry(Type.INVALIDATE_FIELD, 2)
    );

    private static final char ESCAPE_CHARACTER = '\\';
    private static final char PARAMETER_DELIMITER = '|';

    private final Type type;
    private final List<String> parameters;

    /**
     * Constructs a message with given type and parameters
     * @param type Message Type
     * @param parameters Paramateres
     */
    public Message(Type type, Object... parameters) {
        this.type = type;
        this.parameters = new ArrayList<>();
        for (Object parameter : parameters) {
            this.parameters.add(parameter.toString());
        }
    }

    /**
     * Returns the message type
     * @return Message type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns index'th parameter
     * @param index Index
     * @return Index'th parameter
     */
    public String getParameter(int index) {
        return parameters.get(index);
    }

    /**
     * Returns parameters count
     * @return Parameters count
     */
    public int getParametersCnt() {
        return parameters.size();
    }

    /**
     * Serializes the message
     * Escapes parameters delimiters (and escape characters)
     * @return String
     */
    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(type.name());
        for (String parameter : parameters) {
            stringBuilder.append(PARAMETER_DELIMITER);
            for (int i = 0; i < parameter.length(); i++) {
                char c = parameter.charAt(i);
                if (c == ESCAPE_CHARACTER || c == PARAMETER_DELIMITER) {
                    stringBuilder.append(ESCAPE_CHARACTER);
                }
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Deserializes the message
     * @param string String
     * @return Message
     */
    public static Message deserialize(String string) {
        String[] parts = string.split("\\" + PARAMETER_DELIMITER );

        Type type;
        try {
            type = Type.valueOf(parts[0]);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException();
        }

        if (!PARAMETERS_COUNTS.containsKey(type)) {
            throw new RuntimeException();
        }

        if (string.length() == type.name().length()) {
            return new Message(type);
        }

        List<String> parameters = new ArrayList<>();
        boolean escape = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = type.name().length() + 1; ; i++) {
            if (i >= string.length()) {
                parameters.add(stringBuilder.toString());
                break;
            }

            char c = string.charAt(i);
            if (!escape) {
                if (c == ESCAPE_CHARACTER) {
                    escape = true;
                    continue;
                }

                if (c == PARAMETER_DELIMITER) {
                    parameters.add(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                    continue;
                }
            }

            escape = false;
            stringBuilder.append(c);
        }

        if (PARAMETERS_COUNTS.get(type) != parameters.size()) {
            throw new RuntimeException();
        }

        return new Message(type, parameters.toArray());
    }

    /**
     * Returns string representation of the message
     * @return String
     */
    @Override
    public String toString() {
        return serialize();
    }
}
