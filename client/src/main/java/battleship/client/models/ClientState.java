package battleship.client.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClientState {

    private final StringProperty nickname = new SimpleStringProperty("");
    private final BoardState boardState = new BoardState();
    private final BooleanProperty isBoardReady = new SimpleBooleanProperty();
    private final BooleanProperty isOnTurn = new SimpleBooleanProperty();

    private final BooleanProperty isResponding = new SimpleBooleanProperty(true);

    public StringProperty nicknameProperty() {
        return nickname;
    }

    public BoardState getBoard() {
        return boardState;
    }

    public BooleanProperty isBoardReadyProperty() {
        return isBoardReady;
    }

    public BooleanProperty isOnTurnProperty() {
        return isOnTurn;
    }

    public BooleanProperty isRespondingProperty() {
        return isResponding;
    }
}
