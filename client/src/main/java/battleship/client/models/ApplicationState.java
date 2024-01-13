package battleship.client.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ApplicationState {

    private static final boolean IS_DEBUG = true;

    private final BooleanProperty indexDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty lobbyDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty roomDisable = new SimpleBooleanProperty(false);

    private final StringProperty serverAddress = new SimpleStringProperty(IS_DEBUG ? "localhost" : "");
    private final StringProperty serverPort = new SimpleStringProperty(IS_DEBUG ? "50000" : "");
    private final StringProperty nickname = new SimpleStringProperty(IS_DEBUG ? "nickname" : "");
    private final StringProperty roomCode = new SimpleStringProperty(IS_DEBUG ? "7938" : "");

    public BooleanProperty indexDisableProperty() {
        return indexDisable;
    }

    public BooleanProperty lobbyDisableProperty() {
        return lobbyDisable;
    }

    public BooleanProperty roomDisableProperty() {
        return roomDisable;
    }

    public StringProperty serverAddressProperty() {
        return serverAddress;
    }

    public StringProperty serverPortProperty() {
        return serverPort;
    }

    public StringProperty nicknameProperty() {
        return nickname;
    }

    public StringProperty roomCodeProperty() {
        return roomCode;
    }

    public void setControlsDisable(boolean isDisabled) {
        indexDisable.set(isDisabled);
        lobbyDisable.set(isDisabled);
        roomDisable.set(isDisabled);
    }

    public void reset() {
        indexDisable.set(false);
        lobbyDisable.set(false);
        roomDisable.set(false);
        resetRoomCode();
    }

    public void resetRoomCode() {
        roomCode.set("");
    }
}
