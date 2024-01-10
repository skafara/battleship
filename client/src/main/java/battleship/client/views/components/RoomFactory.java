package battleship.client.views.components;

import battleship.client.controllers.Controller;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import battleship.client.views.Board;
import battleship.client.views.components.forms.FormFactory;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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

        Button buttonLeaveRoom = FormFactory.getButton("Leave Room", e -> System.out.println("leave"));
        Button buttonReady = FormFactory.getButton("Ready", e -> System.out.println("ready"));

        vBox.getChildren().addAll(textRoomCode, buttonLeaveRoom, buttonReady);

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

    private static VBox getBoardSide(ClientState clientState, Controller controller, boolean client) {
        VBox vBox = new VBox();
        vBox.setSpacing(SPACING);
        vBox.setAlignment(Pos.CENTER);

        Text textNickname = new Text();
        if (client) {
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

        Board board = new Board(clientState, controller, client);

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

}
