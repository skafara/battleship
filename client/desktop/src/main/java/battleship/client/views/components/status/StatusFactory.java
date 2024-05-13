package battleship.client.views.components.status;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Status Factory
 */
public class StatusFactory {

    private static final int PADDING = 16;
    private static final int MAX_WIDTH = 400;

    /**
     * Constructs a server status
     * @param address Server address
     * @param isResponding Server is responding / Client is connected
     * @return Server Status
     */
    public static ServerStatus getServerStatus(StringProperty address, BooleanProperty isResponding) {
        ServerStatus serverStatus = new ServerStatus(address, isResponding);
        serverStatus.setAlignment(Pos.CENTER_LEFT);
        serverStatus.setPadding(new Insets(PADDING));
        serverStatus.setMaxWidth(MAX_WIDTH);
        return serverStatus;
    }

    /**
     * Constructs an opponent status
     * @param isInRoom Opponent is in room
     * @param isResponding Opponent is responding
     * @return Opponent Status
     */
    public static OpponentStatus getOpponentStatus(BooleanBinding isInRoom, BooleanProperty isResponding) {
        OpponentStatus opponentStatus = new OpponentStatus(isInRoom, isResponding);
        opponentStatus.setAlignment(Pos.CENTER_RIGHT);
        opponentStatus.setPadding(new Insets(PADDING));
        opponentStatus.setMaxWidth(MAX_WIDTH);
        return opponentStatus;
    }

}
