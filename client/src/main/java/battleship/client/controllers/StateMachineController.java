package battleship.client.controllers;

import battleship.client.models.BoardState;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class StateMachineController {

    private final Model model;
    private final StageManager stageManager;

    public StateMachineController(Model model, StageManager stageManager) {
        this.model = model;
        this.stageManager = stageManager;
    }

    public void handleConnTerm(Message message) {
        System.out.println(message.Serialize());
    }

    public void handleOpponentNicknameSet(Message message) {
        model.opponentState.nicknameProperty().set(message.getParameter(0));
    }

    public void handleOpponentBoardReady(Message message) {
        model.opponentState.isBoardReadyProperty().set(true);
    }

    public void handleOpponentRoomLeave(Message message) {
        model.applicationState.roomCodeProperty().set("");
        model.clientState.resetExceptNickname();
        model.opponentState.reset();
        Platform.runLater(() -> stageManager.setScene(StageManager.Scene.Lobby));
    }

    public void handleGameBegin(Message message) {
        System.out.println("game begin");
    }

    public void handleTurnSet(Message message) {
        boolean client = message.getParameter(0).equals("YOU");
        model.clientState.isOnTurnProperty().set(client);
        model.opponentState.isOnTurnProperty().set(!client);
    }

    public void handleOpponentNoResponse(Message message) {
        boolean short_ = message.getParameter(0).equals("SHORT");
        if (short_) {
            model.opponentState.isRespondingProperty().set(false);
        }
        else {
            model.applicationState.roomCodeProperty().set("");
            model.clientState.resetExceptNickname();
            model.opponentState.reset();
            Platform.runLater(() -> stageManager.setScene(StageManager.Scene.Lobby));
        }
    }

    public void handleOpponentTurn(Message message) {
        String field = message.getParameter(0);
        int row = field.charAt(0) - '0';
        int col = field.charAt(1) - '0';
        BoardState boardState = model.clientState.getBoard();
        if (message.getParameter(1).equals("HIT")) {
            boardState.setField(BoardState.Field.HIT, row, col);
        }
        else {
            boardState.setField(BoardState.Field.MISS, row, col);
        }
    }

    public void handleGameEnd(Message message) {
        boolean isWinner = message.getParameter(0).equals("YOU");

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Game End");
            if (isWinner) {
                alert.setContentText("You have won.");
            }
            else {
                alert.setContentText("You have lost.");
            }
            alert.showAndWait();

            model.clientState.resetExceptNickname();
            model.opponentState.resetExceptNickname();

            stageManager.setScene(StageManager.Scene.Room);
        });
    }

    public void handleOpponentRejoin(Message message) {
        model.opponentState.isRespondingProperty().set(true);
    }

    public void handleBoardState(Message message) {
        ClientState clientState = model.opponentState;
        if (message.getParameter(0).equals("YOU")) {
            clientState = model.clientState;
        }

        clientState.isBoardReadyProperty().set(true);
        BoardState boardState = clientState.getBoard();
        for (int i = 1; i < message.getParametersCnt(); i++) {
            String fieldDescription = message.getParameter(i);
            BoardState.Field field = BoardState.Field.valueOf(fieldDescription);
            boardState.setField(field, i - 1);
        }
    }
}
