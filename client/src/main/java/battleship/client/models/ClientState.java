package battleship.client.models;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClientState {

    private final StringProperty nickname = new SimpleStringProperty("");
    private final BooleanBinding isInRoom = Bindings.createBooleanBinding(() -> !nickname.get().isEmpty(), nickname);
    private final BoardState boardState = new BoardState();
    private final BooleanProperty isBoardReady = new SimpleBooleanProperty(false);
    private final BooleanProperty isOnTurn = new SimpleBooleanProperty(false);

    private final BooleanProperty isResponding = new SimpleBooleanProperty(true);

    public StringProperty nicknameProperty() {
        return nickname;
    }

    public BooleanBinding isInRoomBinding() {
        return isInRoom;
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

    public void reset() {
        nickname.set("");
        resetExceptNickname();
    }

    public void resetExceptNickname() {
        boardState.reset();
        isBoardReady.set(false);
        isOnTurn.set(false);
        isResponding.set(true);
    }

}
