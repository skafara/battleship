package battleship.client.views;

import battleship.client.controllers.Controller;
import battleship.client.models.Model;
import battleship.client.views.scenes.Index;
import battleship.client.views.scenes.Lobby;
import battleship.client.views.scenes.Room;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Stage Manager
 */
public class StageManager {

    /**
     * Scene
     */
    public enum Scene {
        Index,
        Lobby,
        Room
    }

    private static final String TITLE = "Battleship";
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    private Stage stage;

    private final Model model = new Model();
    private final Controller controller = new Controller(model);

    /**
     * Constructs a Stage Manager
     */
    public StageManager() {
        controller.setStageManager(this);
    }

    /**
     * Sets a stage to manage
     * @param stage Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setTitle(TITLE);
        this.stage.setMinWidth(MIN_WIDTH);
        this.stage.setMinHeight(MIN_HEIGHT);
    }

    /**
     * Sets a new scene
     * @param scene Scene
     */
    public void setScene(Scene scene) {
        Parent parent = null;
        switch (scene) {
            case Index -> parent = new Index(model, controller);
            case Lobby -> parent = new Lobby(model, controller);
            case Room -> parent = new Room(model, controller);
        }

        stage.setScene(new javafx.scene.Scene(parent, MIN_WIDTH, MIN_HEIGHT));
    }

    /**
     * Sets a JavaFX request to set scene
     * @param scene Scene
     */
    public void setSceneLater(Scene scene) {
        Platform.runLater(() -> setScene(scene));
    }

    /**
     * Shows an alert
     * @param type Alert Type
     * @param header Header message
     * @param content Content message
     */
    public void showAlert(Alert.AlertType type, String header, String content) {
        String typeText = "";
        if (type == Alert.AlertType.ERROR) {
            typeText = "Error";
        } else if (type == Alert.AlertType.INFORMATION) {
            typeText = "Information";
        }

        Alert alert = new Alert(type);
        alert.setTitle(typeText);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets a JavaFX request to show an alert
     * @param type Alert Type
     * @param header Header message
     * @param content Content message
     */
    public void showAlertLater(Alert.AlertType type, String header, String content) {
        Platform.runLater(() -> showAlert(type, header, content));
    }

    /**
     * Shows the stage
     */
    public void showStage() {
        stage.show();
    }

}
