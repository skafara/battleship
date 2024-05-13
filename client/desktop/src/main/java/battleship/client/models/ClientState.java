package battleship.client.models;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Client State
 */
public class ClientState {

    private final StringProperty nickname = new SimpleStringProperty("");
    private final BooleanBinding isInRoom = Bindings.createBooleanBinding(() -> !nickname.get().isEmpty(), nickname);
    private final BoardState boardState = new BoardState();
    private final BooleanProperty isBoardReady = new SimpleBooleanProperty(false);
    private final BooleanProperty isOnTurn = new SimpleBooleanProperty(false);

    private final BooleanProperty isResponding = new SimpleBooleanProperty(false);

    /**
     * Return nickname
     * @return Nickname
     */
    public StringProperty nicknameProperty() {
        return nickname;
    }

    /**
     * Returns "is in room" status
     * @return "Is in room" status
     */
    public BooleanBinding isInRoomBinding() {
        return isInRoom;
    }

    /**
     * Returns board state
     * @return Board state
     */
    public BoardState getBoardState() {
        return boardState;
    }

    /**
     * Returns "board is ready" status
     * @return "Board is ready" status
     */
    public BooleanProperty isBoardReadyProperty() {
        return isBoardReady;
    }

    /**
     * Returns "is on turn" status
     * @return "Is on turn" status
     */
    public BooleanProperty isOnTurnProperty() {
        return isOnTurn;
    }

    /**
     * Returns "is responding/is connected" status
     * @return "Is responding/is connected" status
     */
    public BooleanProperty isRespondingProperty() {
        return isResponding;
    }

    /**
     * Reset model
     * (does not reset "is responding" status)
     */
    public void reset() {
        nickname.set("");
        resetExceptNickname();
    }

    /**
     * Reset model except nickname
     * (does not reset "is responding" status)
     */
    public void resetExceptNickname() {
        boardState.reset();
        isBoardReady.set(false);
        isOnTurn.set(false);
    }

}
