package battleship.client.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Application State
 */
public class ApplicationState {

    private static final boolean IS_DEBUG = false;

    private final BooleanProperty indexDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty lobbyDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty roomDisable = new SimpleBooleanProperty(false);

    private final StringProperty serverAddress = new SimpleStringProperty(IS_DEBUG ? "localhost" : "");
    private final StringProperty serverPort = new SimpleStringProperty(IS_DEBUG ? "50000" : "");
    private final StringProperty nickname = new SimpleStringProperty(IS_DEBUG ? "nickname" : "");
    private final StringProperty roomCode = new SimpleStringProperty(IS_DEBUG ? "7938" : "");

    /**
     * Returns "index scene disable" status
     * @return "Index scene disable" status
     */
    public BooleanProperty indexDisableProperty() {
        return indexDisable;
    }

    /**
     * Returns "lobby scene disable" status
     * @return "Lobby scene disable" status
     */
    public BooleanProperty lobbyDisableProperty() {
        return lobbyDisable;
    }

    /**
     * Returns "room scene disable" status
     * @return "Room scene disable" status
     */
    public BooleanProperty roomDisableProperty() {
        return roomDisable;
    }

    /**
     * Returns server address
     * @return Server address
     */
    public StringProperty serverAddressProperty() {
        return serverAddress;
    }

    /**
     * Returns server port
     * @return Server port
     */
    public StringProperty serverPortProperty() {
        return serverPort;
    }

    /**
     * Returns client's nickname
     * @return Client's nickname
     */
    public StringProperty nicknameProperty() {
        return nickname;
    }

    /**
     * Returns room code
     * @return Room code
     */
    public StringProperty roomCodeProperty() {
        return roomCode;
    }

    /**
     * Sets all controls disable status
     * @param isDisabled Boolean
     */
    public void setControlsDisable(boolean isDisabled) {
        indexDisable.set(isDisabled);
        lobbyDisable.set(isDisabled);
        roomDisable.set(isDisabled);
    }

    /**
     * Resets model (except client's nickname, server address and port)
     */
    public void reset() {
        setControlsDisable(false);
        resetRoomCode();
    }

    /**
     * Resets room code
     */
    public void resetRoomCode() {
        roomCode.set("");
    }
}
