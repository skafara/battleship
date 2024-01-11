package battleship.client.views.components;

import battleship.client.controllers.Controller;
import battleship.client.models.ApplicationState;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import battleship.client.views.Board;
import battleship.client.views.StageManager;
import battleship.client.views.components.forms.FormFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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

        ApplicationState applicationState = model.applicationState;

        Text textRoomCode = new Text();
        textRoomCode.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("Room %s", applicationState.roomCodeProperty().get()),
                        model.applicationState.roomCodeProperty()
                )
        );

        Button buttonLeaveRoom = FormFactory.getButton("Leave Room", e -> handleButtonLeaveRoom(model, controller));
        buttonLeaveRoom.disableProperty().bind(applicationState.roomDisableProperty());

        vBox.getChildren().addAll(textRoomCode, buttonLeaveRoom);
        if (!model.clientState.isBoardReadyProperty().get()) {
            Button buttonReady = FormFactory.getButton("Ready", e -> handleButtonReady(model, controller));
            buttonReady.disableProperty().bind(applicationState.roomDisableProperty());
            vBox.getChildren().add(buttonReady);
        }

        return vBox;
    }

    public static HBox getBoards(Model model, Controller controller) {
        HBox hBox = new HBox();
        hBox.setSpacing(BOARDS_SPACING);
        hBox.setAlignment(Pos.CENTER);

        BooleanBinding isInGameBinding = Bindings.createBooleanBinding(() -> {
            return model.clientState.isBoardReadyProperty().get() && model.opponentState.isBoardReadyProperty().get();
        }, model.clientState.isBoardReadyProperty(), model.opponentState.isBoardReadyProperty());

        hBox.getChildren().addAll(
                getBoardSide(model, controller, true, isInGameBinding),
                getBoardSide(model, controller, false, isInGameBinding)
        );

        return hBox;
    }

    private static VBox getBoardSide(Model model, Controller controller, boolean isClient, BooleanBinding isInGame) {
        VBox vBox = new VBox();
        vBox.setSpacing(SPACING);
        vBox.setAlignment(Pos.CENTER);

        ClientState clientState = isClient ? model.clientState : model.opponentState;;

        Text textNickname = new Text();
        if (isClient) {
            textNickname.textProperty().bind(clientState.nicknameProperty());
        }
        else {
            textNickname.textProperty().bind(
                    Bindings.createStringBinding(
                            () -> {
                                if (!clientState.isInRoomBinding().get()) {
                                    return "...";
                                }
                                else {
                                    return clientState.nicknameProperty().get();
                                }
                            },
                            clientState.isInRoomBinding()
                    )
            );
        }

        Board board = new Board(model, controller, isClient, isInGame);

        Text textDescription = new Text();
        textDescription.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            if (!isInGame.get()) {
                                if (clientState.isBoardReadyProperty().get()) {
                                    return "Ready";
                                } else if (clientState.isInRoomBinding().get()) {
                                    return "Not Ready";
                                }
                            }
                            else {
                                if (isClient) {
                                    if (clientState.isOnTurnProperty().get()) {
                                        return "Your Turn";
                                    }
                                }
                                else {
                                    if (clientState.isOnTurnProperty().get()) {
                                        return "Opponent's Turn";
                                    }
                                }
                            }
                            return "";
                        },
                        isInGame, clientState.isInRoomBinding(), clientState.isBoardReadyProperty(), clientState.isOnTurnProperty()
                )
        );

        vBox.getChildren().addAll(textNickname, board, textDescription);
        return vBox;
    }

    private static void handleButtonLeaveRoom(Model model, Controller controller) {
        ApplicationState applicationState = model.applicationState;
        applicationState.roomDisableProperty().set(true);

        CompletableFuture<Void> future = controller.leaveRoom();
        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                applicationState.roomDisableProperty().set(false);
                controller.getStageManager().setSceneLater(StageManager.Scene.Lobby);
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

            applicationState.roomDisableProperty().set(false);
        });
    }

    private static void handleButtonReady(Model model, Controller controller) {
        ApplicationState applicationState = model.applicationState;
        applicationState.roomDisableProperty().set(true);

        CompletableFuture<Void> future = controller.boardReady(model.clientState.getBoard());
        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                applicationState.roomDisableProperty().set(false);
                controller.getStageManager().setSceneLater(StageManager.Scene.Room);
                return;
            }

            StageManager stageManager = controller.getStageManager();
            switch (exception) {
                case IllegalArgumentException e -> handleIllegalBoard(stageManager);
                case IllegalStateException e -> handleNotYourTurn(stageManager);
                /*case IOException e -> {
                    Platform.runLater(() -> handleIO(e));
                }*/
                default -> {
                    //
                }
            }

            applicationState.roomDisableProperty().set(false);
        });
    }

    private static void handleIllegalBoard(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Invalid Board", "Check the validity of the game board.");
    }

    private static void handleNotYourTurn(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Not Your Turn", "Wait for the opponent's turn.");
    }

}
