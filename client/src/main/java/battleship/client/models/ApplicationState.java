package battleship.client.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ApplicationState {

    private final BooleanProperty buttonConnectDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty lobbyDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty buttonLeaveRoomDisable = new SimpleBooleanProperty(false);
    private final BooleanProperty buttonReadyDisable = new SimpleBooleanProperty(false);

    private final StringProperty serverAddress = new SimpleStringProperty("10.0.1.62");
    private final StringProperty serverPort = new SimpleStringProperty("50000");
    private final StringProperty nickname = new SimpleStringProperty("standa");
    private final StringProperty roomCode = new SimpleStringProperty("7938");

    public BooleanProperty buttonConnectDisableProperty() {
        return buttonConnectDisable;
    }

    public BooleanProperty lobbyDisableProperty() {
        return lobbyDisable;
    }

    public BooleanProperty buttonLeaveRoomDisableProperty() {
        return buttonLeaveRoomDisable;
    }

    public BooleanProperty buttonReadyDisableProperty() {
        return buttonReadyDisable;
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

}
