package battleship.client.models;

/**
 * Application Model
 */
public class Model {

    /** Application state */
    public final ApplicationState applicationState = new ApplicationState();
    /** Client state */
    public final ClientState clientState = new ClientState();
    /** Opponent state */
    public final ClientState opponentState = new ClientState();

    /**
     * Constructs an Application Model
     */
    public Model() {
        clientState.nicknameProperty().bindBidirectional(applicationState.nicknameProperty());
    }

    /**
     * Resets the application model (except client's nickname)
     */
    public void reset() {
        applicationState.reset();
        clientState.resetExceptNickname();
        opponentState.reset();
    }
}
