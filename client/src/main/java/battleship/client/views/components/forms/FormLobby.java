package battleship.client.views.components.forms;

import battleship.client.controllers.Controller;
import battleship.client.controllers.exceptions.NotExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.ApplicationState;
import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class FormLobby extends HBox {

    private static final int SPACING = 80;
    private static final int ITEM_MAX_WIDTH = 160;

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

        FormInputField inputField = FormFactory.getBidirectionalyBoundFormInputField("Room Code", applicationState.roomCodeProperty());
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
                Platform.runLater(() -> controller.getStageManager().setScene(StageManager.Scene.Room));
                applicationState.lobbyDisableProperty().set(false);
                return;
            }

            switch (exception) {
                case ReachedLimitException e -> Platform.runLater(this::handleRoomFull);
                case NotExistsException e -> Platform.runLater(this::handleNotExists);
                /*case IOException e -> {
                    Platform.runLater(() -> handleIO(e));
                }*/
                default -> {
                    //
                }
            }

            applicationState.lobbyDisableProperty().set(false);
        });
    }

    private void handleButtonCreateRoom(ApplicationState applicationState, Controller controller) {
        applicationState.lobbyDisableProperty().set(true);

        CompletableFuture<Void> future = controller.createRoom();

        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                Platform.runLater(() -> controller.getStageManager().setScene(StageManager.Scene.Room));
                applicationState.lobbyDisableProperty().set(false);
                return;
            }

            switch (exception) {
                case ReachedLimitException e -> Platform.runLater(() -> handleRoomLimit(e));
                /*case IOException e -> {
                    Platform.runLater(() -> handleIO(e));
                }*/
                default -> {
                    //
                }
            }

            applicationState.lobbyDisableProperty().set(false);
        });
    }

    private void handleRoomLimit(ReachedLimitException e) { // TODO alert factory?
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(String.format("Rooms count limit (%d) reached", e.getLimit()));
        alert.setContentText("Please try again later.");
        alert.showAndWait();
    }

    private void handleRoomFull() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Room is full");
        alert.setContentText("Please join a not full room.");
        alert.showAndWait();
    }

    private void handleNotExists() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Room does not exist");
        alert.setContentText("Check the validity of the room code.");
        alert.showAndWait();
    }

}
