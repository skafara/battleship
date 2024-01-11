package battleship.client.views.components.forms;

import battleship.client.controllers.Controller;
import battleship.client.controllers.exceptions.ExistsException;
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

        FormInputField fieldAddress = FormFactory.getBidirectionalyBoundFormInputField("Server Address", applicationState.serverAddressProperty());
        fieldAddress.disableProperty().bind(applicationState.indexDisableProperty());

        FormInputField fieldPort = FormFactory.getBidirectionalyBoundFormInputField("Server Port", applicationState.serverPortProperty());
        fieldPort.disableProperty().bind(applicationState.indexDisableProperty());

        FormInputField fieldNickname = FormFactory.getBidirectionalyBoundFormInputField("Nickname", applicationState.nicknameProperty());
        fieldNickname.disableProperty().bind(applicationState.indexDisableProperty());

        getChildren().addAll(
                fieldAddress,
                fieldPort,
                fieldNickname,
                constructButton(applicationState, controller)
        );
    }

    private Button constructButton(ApplicationState applicationState, Controller controller) {
        Button button = FormFactory.getButton("Connect", (e) -> handleButtonConnect(applicationState, controller));
        button.disableProperty().bind(applicationState.indexDisableProperty());
        return button;
    }

    private void handleButtonConnect(ApplicationState applicationState, Controller controller) {
        applicationState.indexDisableProperty().set(true);

        CompletableFuture<Void> future = controller.connect(
                applicationState.serverAddressProperty().get(),
                applicationState.serverPortProperty().get(),
                applicationState.nicknameProperty().get()
        );

        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                applicationState.indexDisableProperty().set(false);
                return;
            }

            StageManager stageManager = controller.getStageManager();
            switch (exception) {
                case ExistsException e -> handleNicknameExists(stageManager);
                case ReachedLimitException e -> handleLimit(stageManager, e);
                case IllegalArgumentException e -> handleIllegalArgument(stageManager);
                case UnknownHostException e -> handleUnknownHost(stageManager);
                case SocketTimeoutException e -> handleSocketTimeout(stageManager);
                case IOException e -> handleIO(stageManager, e);
                default -> {
                    //
                }
            }

            applicationState.indexDisableProperty().set(false);
        });
    }

    private void handleNicknameExists(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.INFORMATION, "Nickname Exists", "Please use different nickname.");
    }

    private void handleLimit(StageManager stageManager, ReachedLimitException e) {
        stageManager.showAlertLater(Alert.AlertType.INFORMATION, String.format("Clients Count Limit (%d) Reached", e.getLimit()), "Please try again later.");
    }

    private void handleIllegalArgument(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Invalid Input", "Check the validity of the server address and port.");
    }

    private void handleUnknownHost(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Unknown Host", "Check the validity of the server address.");
    }

    private void handleSocketTimeout(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Timed Out Connecting", "Check the validity of the server address and port and try again.");
    }

    private void handleIO(StageManager stageManager, IOException exception) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, exception.getMessage(), "Check the validity of the server address and port and try again.");
    }
}
