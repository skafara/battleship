package battleship.client.views.components.status;

import battleship.client.views.StageManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class StatusFactory {

    private static final int PADDING = 16;
    private static final int MAX_WIDTH = 400;

    public static ServerStatus getServerStatus(StringProperty address, BooleanProperty isResponding) {
        ServerStatus serverStatus = new ServerStatus(address, isResponding);
        serverStatus.setAlignment(Pos.CENTER_LEFT);
        serverStatus.setPadding(new Insets(PADDING));
        serverStatus.setMaxWidth(MAX_WIDTH);
        return serverStatus;
    }

    public static OpponentStatus getOpponentStatus(BooleanProperty isResponding) {
        OpponentStatus opponentStatus = new OpponentStatus(isResponding);
        opponentStatus.setAlignment(Pos.CENTER_RIGHT);
        opponentStatus.setPadding(new Insets(PADDING));
        opponentStatus.setMaxWidth(MAX_WIDTH);
        return opponentStatus;
    }

}
