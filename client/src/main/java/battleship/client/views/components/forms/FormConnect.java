package battleship.client.views.components.forms;

import battleship.client.controllers.Controller;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.ApplicationState;
import battleship.client.views.StageManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class FormConnect extends VBox {

    private static final int MAX_WIDTH = 160;

    public FormConnect(ApplicationState applicationState, Controller controller) {
        construct(applicationState, controller);
    }

    private void construct(ApplicationState applicationState, Controller controller) {
        setSpacing(FormFactory.SPACING);
        setMaxWidth(MAX_WIDTH);
        setAlignment(Pos.CENTER);

        getChildren().addAll(
                FormFactory.getBidirectionalyBoundFormInputField("Server Address", applicationState.serverAddressProperty()),
                FormFactory.getBidirectionalyBoundFormInputField("Server Port", applicationState.serverPortProperty()),
                FormFactory.getBidirectionalyBoundFormInputField("Nickname", applicationState.nicknameProperty()),
                constructButton(applicationState, controller)
        );
    }

    private Button constructButton(ApplicationState applicationState, Controller controller) {
        Button button = FormFactory.getButton("Connect", (e) -> handleButtonConnect(applicationState, controller));
        button.disableProperty().bind(applicationState.buttonConnectDisableProperty());
        return button;
    }

    private void handleButtonConnect(ApplicationState applicationState, Controller controller) {
        applicationState.buttonConnectDisableProperty().set(true);

        CompletableFuture<Void> future = controller.connect(
                applicationState.serverAddressProperty().get(),
                applicationState.serverPortProperty().get(),
                applicationState.nicknameProperty().get()
        );

        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                Platform.runLater(() -> {
                    controller.getStageManager().setScene(StageManager.Scene.Lobby);
                });
                applicationState.buttonConnectDisableProperty().set(false);
                return;
            }

            switch (exception) {
                case ReachedLimitException e -> Platform.runLater(() -> handleLimit(e));
                case IllegalArgumentException e -> Platform.runLater(this::handleIllegalArgument);
                case UnknownHostException e -> Platform.runLater(this::handleUnknownHost);
                case SocketTimeoutException e -> Platform.runLater(this::handleSocketTimeout);
                case IOException e -> Platform.runLater(() -> handleIO(e));
                default -> {
                    //
                }
            }

            applicationState.buttonConnectDisableProperty().set(false);
        });
    }

    private void handleLimit(ReachedLimitException e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(String.format("Clients count limit (%d) reached", e.getLimit()));
        alert.setContentText("Please try again later.");
        alert.showAndWait();
    }

    private void handleIllegalArgument() { // TODO alert factory?
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText("Check the validity of the server address and port.");
        alert.showAndWait();
    }

    private void handleUnknownHost() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Unknown Host");
        alert.setContentText("Check the validity of the server address.");
        alert.showAndWait();
    }

    private void handleSocketTimeout() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Timed out connecting");
        alert.setContentText("Check the validity of the server address and port and try again.");
        alert.showAndWait();
    }

    private void handleIO(IOException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(exception.getMessage());
        alert.setContentText("Check the validity of the server address and port and try again.");
        alert.showAndWait();
    }
}
