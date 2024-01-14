package battleship.client.controllers;

import battleship.client.controllers.messages.Message;
import battleship.client.models.BoardState;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StateMachineController {

    private final Model model;
    private final StageManager stageManager;

    public StateMachineController(Model model, StageManager stageManager) {
        this.model = model;
        this.stageManager = stageManager;
    }

    public void handleConnTerm(Message message) {

    }

    public void handleOpponentNicknameSet(Message message) {
        model.opponentState.nicknameProperty().set(message.getParameter(0));
    }

    public void handleOpponentBoardReady(Message message) {
        model.opponentState.isBoardReadyProperty().set(true);
    }

    public void handleOpponentRoomLeave(Message message) {
        model.applicationState.resetRoomCode();
        model.clientState.resetExceptNickname();
        model.opponentState.reset();

        stageManager.showAlertLater(Alert.AlertType.INFORMATION, "Opponent Left Room", "Your opponent has left the room.");
        stageManager.setSceneLater(StageManager.Scene.Lobby);
    }

    public void handleGameBegin(Message message) {
        //
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
            model.applicationState.resetRoomCode();
            model.clientState.resetExceptNickname();
            model.opponentState.reset();

            stageManager.showAlertLater(Alert.AlertType.INFORMATION, "Opponent Not Responding", "Your opponent has been disconnected for not responding to the server for a long time.");
            stageManager.setSceneLater(StageManager.Scene.Lobby);
        }
    }

    public void handleOpponentTurn(Message message) {
        String field = message.getParameter(0);
        int row = field.charAt(0) - '0';
        int col = field.charAt(1) - '0';
        BoardState boardState = model.clientState.getBoardState();
        if (message.getParameter(1).equals("HIT")) {
            boardState.setField(BoardState.Field.HIT, row, col);
        }
        else {
            boardState.setField(BoardState.Field.MISS, row, col);
        }
    }

    public void handleGameEnd(Message message) {
        boolean isWinner = message.getParameter(0).equals("YOU");

        model.clientState.resetExceptNickname();
        model.opponentState.resetExceptNickname();

        String alertContent = isWinner ? "You have won." : "You have lost.";
        stageManager.showAlertLater(Alert.AlertType.INFORMATION, "Game End", alertContent);
        stageManager.setSceneLater(StageManager.Scene.Room);
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
        BoardState boardState = clientState.getBoardState();
        for (int i = 1; i < message.getParametersCnt(); i++) {
            String fieldDescription = message.getParameter(i);
            BoardState.Field field = BoardState.Field.valueOf(fieldDescription);
            boardState.setField(field, i - 1);
        }
    }

    public void handleInvalidateField(Message message) {
        BoardState boardState = model.opponentState.getBoardState();
        if (message.getParameter(0).equals("YOU")) {
            boardState = model.clientState.getBoardState();
        }
        String field = message.getParameter(1);
        int row = field.charAt(0) - '0';
        int col = field.charAt(1) - '0';
        boardState.setField(BoardState.Field.INVALIDATED, row, col);
    }
}
