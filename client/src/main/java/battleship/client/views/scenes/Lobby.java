package battleship.client.views.scenes;

import battleship.client.controllers.Controller;
import battleship.client.models.ApplicationState;
import battleship.client.models.Model;
import battleship.client.views.components.MenuBar;
import battleship.client.views.components.forms.FormLobby;
import battleship.client.views.components.status.StatusFactory;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Lobby Scene
 */
public class Lobby extends BorderPane {

    /**
     * Constructs a Lobby Scene
     * @param model Model
     * @param controller Controller
     */
    public Lobby(Model model, Controller controller) {
        construct(model, controller);
    }

    private void construct(Model model, Controller controller) {
        ApplicationState applicationState = model.applicationState;

        VBox vBox = new VBox(new FormLobby(applicationState, controller));
        vBox.setAlignment(Pos.CENTER);
        setTop(new MenuBar());
        setCenter(vBox);
        setBottom(StatusFactory.getServerStatus(applicationState.serverAddressProperty(), model.clientState.isRespondingProperty()));
    }

}
