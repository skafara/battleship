package battleship.client.views.scenes;

import battleship.client.controllers.Controller;
import battleship.client.models.Model;
import battleship.client.views.components.MenuBar;
import battleship.client.views.components.forms.FormConnect;
import javafx.scene.layout.BorderPane;

/**
 * Index Scene
 */
public class Index extends BorderPane {

    /**
     * Constructs an Index Scene
     * @param model Model
     * @param controller Controller
     */
    public Index(Model model, Controller controller) {
        construct(model, controller);
    }

    private void construct(Model model, Controller controller) {
        setTop(new MenuBar());
        setCenter(new FormConnect(model.applicationState, controller));
    }

}
