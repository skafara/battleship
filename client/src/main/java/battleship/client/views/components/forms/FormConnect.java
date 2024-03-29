package battleship.client.views.components.forms;

import battleship.client.controllers.Controller;
import battleship.client.controllers.exceptions.ExistsException;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.ApplicationState;
import battleship.client.views.StageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Connect to the server form
 */
public class FormConnect extends VBox {

    private static final int MAX_WIDTH = 160;

    /**
     * Constructs a connect to the server form
     * @param applicationState Application state
     * @param controller Controller
     */
    public FormConnect(ApplicationState applicationState, Controller controller) {
        construct(applicationState, controller);
    }

    private void construct(ApplicationState applicationState, Controller controller) {
        setSpacing(FormFactory.SPACING);
        setMaxWidth(MAX_WIDTH);
        setAlignment(Pos.CENTER);

        FormInputField fieldAddress = FormFactory.getBidirectionallyBoundFormInputField("Server Address", applicationState.serverAddressProperty());
        fieldAddress.disableProperty().bind(applicationState.indexDisableProperty());

        FormInputField fieldPort = FormFactory.getBidirectionallyBoundFormInputField("Server Port", applicationState.serverPortProperty());
        fieldPort.disableProperty().bind(applicationState.indexDisableProperty());

        FormInputField fieldNickname = FormFactory.getBidirectionallyBoundFormInputField("Nickname", applicationState.nicknameProperty());
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
            try {
                throw exception;
            }
            catch (ExistsException e) {
                handleNicknameExists(stageManager);
            }
            catch (ReachedLimitException e) {
                handleLimit(stageManager, e);
            }
            catch (IllegalArgumentException e) {
                handleIllegalArgument(stageManager);
            }
            catch (UnknownHostException e) {
                handleUnknownHost(stageManager);
            }
            catch (SocketTimeoutException | TimeoutException e) {
                handleTimeout(stageManager);
            }
            catch (IOException e) {
                handleIO(stageManager, e);
            }
            catch (Throwable e) {
                //
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

    private void handleTimeout(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Timed Out Connecting", "Check the validity of the server address and port and try again.");
    }

    private void handleIO(StageManager stageManager, IOException exception) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, exception.getMessage(), "Check the validity of the server address and port and try again.");
    }
}
