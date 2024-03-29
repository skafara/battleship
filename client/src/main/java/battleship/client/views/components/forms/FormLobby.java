package battleship.client.views.components.forms;

import battleship.client.controllers.Controller;
import battleship.client.controllers.exceptions.NotExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.ApplicationState;
import battleship.client.views.StageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;

/**
 * Lobby form
 */
public class FormLobby extends HBox {

    private static final int SPACING = 80;
    private static final int ITEM_MAX_WIDTH = 160;

    /**
     * Constructs a lobby form
     * @param applicationState Application state
     * @param controller Controller
     */
    public FormLobby(ApplicationState applicationState, Controller controller) {
        construct(applicationState, controller);
    }

    private void construct(ApplicationState applicationState, Controller controller) {
        setAlignment(Pos.CENTER);
        setSpacing(SPACING);

        getChildren().addAll(
                constructFormJoin(applicationState, controller),
                constructFormCreate(applicationState, controller)
        );
    }

    private VBox constructFormJoin(ApplicationState applicationState, Controller controller) {
        VBox vBox = new VBox();
        vBox.setSpacing(FormFactory.SPACING);
        vBox.setAlignment(Pos.CENTER);

        FormInputField inputField = FormFactory.getBidirectionallyBoundFormInputField("Room Code", applicationState.roomCodeProperty());
        inputField.getTextField().disableProperty().bind(applicationState.lobbyDisableProperty());

        Button button = FormFactory.getButton("Join Room", (e) -> handleButtonJoinRoom(applicationState, controller));
        button.disableProperty().bind(applicationState.lobbyDisableProperty());

        vBox.getChildren().addAll(inputField, button);
        return vBox;
    }

    private VBox constructFormCreate(ApplicationState applicationState, Controller controller) {
        VBox vBox = new VBox();
        vBox.setMinWidth(ITEM_MAX_WIDTH);
        vBox.setAlignment(Pos.CENTER);

        Button button = FormFactory.getButton("Create Room", (e) -> handleButtonCreateRoom(applicationState, controller));
        button.disableProperty().bind(applicationState.lobbyDisableProperty());

        vBox.getChildren().add(button);
        return vBox;
    }

    private void handleButtonJoinRoom(ApplicationState applicationState, Controller controller) {
        applicationState.lobbyDisableProperty().set(true);

        CompletableFuture<Void> future = controller.joinRoom(applicationState.roomCodeProperty().get());
        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                applicationState.lobbyDisableProperty().set(false);
                controller.getStageManager().setSceneLater(StageManager.Scene.Room);
                return;
            }

            StageManager stageManager = controller.getStageManager();
            try {
                throw exception;
            }
            catch (IllegalArgumentException e) {
                handleIllegalArgument(stageManager);
            }
            catch (ReachedLimitException e) {
                handleRoomFull(stageManager);
            }
            catch (NotExistsException e) {
                handleNotExists(stageManager);
            }
            catch (Throwable e) {
                //
            }

            applicationState.lobbyDisableProperty().set(false);
        });
    }

    private void handleButtonCreateRoom(ApplicationState applicationState, Controller controller) {
        applicationState.lobbyDisableProperty().set(true);

        CompletableFuture<Void> future = controller.createRoom();
        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                controller.getStageManager().setSceneLater(StageManager.Scene.Room);
                applicationState.lobbyDisableProperty().set(false);
                return;
            }

            StageManager stageManager = controller.getStageManager();
            try {
                throw exception;
            }
            catch (ReachedLimitException e) {
                handleRoomLimit(stageManager, e);
            }
            catch (Throwable e) {
                //
            }

            applicationState.lobbyDisableProperty().set(false);
        });
    }

    private void handleIllegalArgument(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Invalid Room Code", "Check the validity of the room code format.");
    }

    private void handleRoomLimit(StageManager stageManager, ReachedLimitException e) {
        stageManager.showAlertLater(Alert.AlertType.INFORMATION, String.format("Rooms Count Limit (%d) Reached", e.getLimit()), "Please try again later.");
    }

    private void handleRoomFull(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Room Is Full", "Please join a not full room.");
    }

    private void handleNotExists(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Room Does Not Exist", "Check the validity of the room code.");
    }

}
