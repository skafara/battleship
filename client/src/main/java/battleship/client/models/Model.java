package battleship.client.models;

public class Model {

    public final ApplicationState applicationState = new ApplicationState();
    public final ClientState clientState = new ClientState();
    public final ClientState opponentState = new ClientState();

    public Model() {
        clientState.nicknameProperty().bindBidirectional(applicationState.nicknameProperty());
    }

    public void reset() {
        applicationState.reset();
        clientState.resetExceptNickname();
        opponentState.reset();
    }
}
