package battleship.client.views.scenes;

import battleship.client.controllers.Controller;
import battleship.client.models.Model;
import battleship.client.views.components.MenuBar;
import battleship.client.views.components.RoomFactory;
import battleship.client.views.components.status.StatusFactory;
import javafx.scene.layout.BorderPane;

/**
 * Room Scene
 */
public class Room extends BorderPane {

    /**
     * Constructs a Room Scene
     * @param model Model
     * @param controller Controller
     */
    public Room(Model model, Controller controller) {
        construct(model, controller);
    }

    private void construct(Model model, Controller controller) {
        setTop(new MenuBar());
        setCenter(constructCenter(model, controller));
        setBottom(constructBottom(model));
    }

    private BorderPane constructCenter(Model model, Controller controller) {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(RoomFactory.getControlPanel(model, controller));
        borderPane.setCenter(RoomFactory.getBoards(model, controller));
        return borderPane;
    }

    private BorderPane constructBottom(Model model) {
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(StatusFactory.getServerStatus(model.applicationState.serverAddressProperty(), model.clientState.isRespondingProperty()));
        borderPane.setRight(StatusFactory.getOpponentStatus(model.opponentState.isInRoomBinding(), model.opponentState.isRespondingProperty()));
        return borderPane;
    }

}
