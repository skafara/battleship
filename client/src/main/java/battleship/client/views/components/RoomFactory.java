package battleship.client.views.components;

import battleship.client.controllers.Controller;
import battleship.client.controllers.exceptions.ReachedLimitException;
import battleship.client.models.ApplicationState;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import battleship.client.views.Board;
import battleship.client.views.StageManager;
import battleship.client.views.components.forms.FormFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class RoomFactory {

    private static final int SPACING = 8;
    private static final int CONTROL_PANEL_PADDING_TOP = 32;
    private static final int BOARDS_SPACING = 80;

    public static VBox getControlPanel(Model model, Controller controller) {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(SPACING);
        vBox.setPadding(new Insets(CONTROL_PANEL_PADDING_TOP, 0, 0, 0));

        Text textRoomCode = new Text();
        textRoomCode.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("Room %s", model.applicationState.roomCodeProperty().get()),
                        model.applicationState.roomCodeProperty()
                )
        );

        Button buttonLeaveRoom = FormFactory.getButton("Leave Room", e -> handleButtonLeaveRoom(model, controller));
        buttonLeaveRoom.disableProperty().bind(model.applicationState.buttonLeaveRoomDisableProperty());

        vBox.getChildren().addAll(textRoomCode, buttonLeaveRoom);
        if (!model.clientState.isBoardReadyProperty().get()) {
            Button buttonReady = FormFactory.getButton("Ready", e -> handleButtonReady(model, controller));
            vBox.getChildren().add(buttonReady);
        }

        return vBox;
    }

    public static HBox getBoards(Model model, Controller controller) {
        HBox hBox = new HBox();
        hBox.setSpacing(BOARDS_SPACING);
        hBox.setAlignment(Pos.CENTER);

        hBox.getChildren().addAll(
                getBoardSide(model.clientState, controller, true),
                getBoardSide(model.opponentState, controller, false)
        );

        return hBox;
    }

    private static VBox getBoardSide(ClientState clientState, Controller controller, boolean isClient) {
        VBox vBox = new VBox();
        vBox.setSpacing(SPACING);
        vBox.setAlignment(Pos.CENTER);

        Text textNickname = new Text();
        if (isClient) {
            textNickname.textProperty().bind(clientState.nicknameProperty());
        }
        else {
            textNickname.textProperty().bind(
                    Bindings.createStringBinding(
                            () -> {
                                if (clientState.nicknameProperty().get().isEmpty()) {
                                    return "...";
                                }
                                else {
                                    return clientState.nicknameProperty().get();
                                }
                            },
                            clientState.nicknameProperty()
                    )
            );
        }

        Board board = new Board(clientState, controller, isClient);

        Text textDescription = new Text();
        textDescription.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            if (clientState.isBoardReadyProperty().get()) {
                                return "Ready";
                            }
                            else {
                                return "Not Ready";
                            }
                        },
                        clientState.isBoardReadyProperty(), clientState.isOnTurnProperty()
                )
        );

        vBox.getChildren().addAll(textNickname, board, textDescription);
        return vBox;
    }

    private static void handleButtonLeaveRoom(Model model, Controller controller) {
        ApplicationState applicationState = model.applicationState;
        applicationState.buttonLeaveRoomDisableProperty().set(true);

        CompletableFuture<Void> future = controller.leaveRoom();

        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                Platform.runLater(() -> controller.getStageManager().setScene(StageManager.Scene.Lobby));
                applicationState.buttonLeaveRoomDisableProperty().set(false);
                return;
            }

            switch (exception) {
                /*case IOException e -> {
                    Platform.runLater(() -> handleIO(e));
                }*/
                default -> {
                    //
                }
            }

            applicationState.buttonLeaveRoomDisableProperty().set(false);
        });
    }

    private static void handleButtonReady(Model model, Controller controller) {
        ApplicationState applicationState = model.applicationState;
        applicationState.buttonReadyDisableProperty().set(true);

        CompletableFuture<Void> future = controller.boardReady(model.clientState.getBoard());

        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                applicationState.buttonReadyDisableProperty().set(false);
                Platform.runLater(() -> controller.getStageManager().setScene(StageManager.Scene.Room));
                return;
            }

            switch (exception) {
                case IllegalArgumentException e -> Platform.runLater(RoomFactory::handleIllegalBoard);
                /*case IOException e -> {
                    Platform.runLater(() -> handleIO(e));
                }*/
                default -> {
                    //
                }
            }

            applicationState.buttonReadyDisableProperty().set(false);
        });
    }

    private static void handleIllegalBoard() { // TODO factory?
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Board");
        alert.setContentText("Check the validity of the game board.");
        alert.showAndWait();
    }

}
