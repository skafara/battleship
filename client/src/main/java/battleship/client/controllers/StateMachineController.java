package battleship.client.controllers;

import battleship.client.controllers.messages.Message;
import battleship.client.models.BoardState;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.scene.control.Alert;

/**
 * State Machine Controller
 */
public class StateMachineController {

    private final Model model;
    private final StageManager stageManager;

    /**
     * Constructs a state machine controller
     * @param model Model
     * @param stageManager Stage Manager
     */
    public StateMachineController(Model model, StageManager stageManager) {
        this.model = model;
        this.stageManager = stageManager;
    }

    /**
     * Handles Connection Terminated
     * @param message Message
     */
    public void handleConnTerm(Message message) {

    }

    /**
     * Handles Opponent Nickname Set
     * @param message Message
     */
    public void handleOpponentNicknameSet(Message message) {
        model.opponentState.nicknameProperty().set(message.getParameter(0));
    }

    /**
     * Handles Opponent Board Ready
     * @param message Message
     */
    public void handleOpponentBoardReady(Message message) {
        model.opponentState.isBoardReadyProperty().set(true);
    }

    /**
     * Handles Opponent Room Leave
     * @param message Message
     */
    public void handleOpponentRoomLeave(Message message) {
        model.applicationState.resetRoomCode();
        model.clientState.resetExceptNickname();
        model.opponentState.reset();

        stageManager.showAlertLater(Alert.AlertType.INFORMATION, "Opponent Left Room", "Your opponent has left the room.");
        stageManager.setSceneLater(StageManager.Scene.Lobby);
    }

    /**
     * Handles Game Begin
     * @param message Message
     */
    public void handleGameBegin(Message message) {
        //
    }

    /**
     * Handles Turn Set
     * @param message Message
     */
    public void handleTurnSet(Message message) {
        boolean client = message.getParameter(0).equals("YOU");
        model.clientState.isOnTurnProperty().set(client);
        model.opponentState.isOnTurnProperty().set(!client);
    }

    /**
     * Handles Opponent No Response
     * @param message Message
     */
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

    /**
     * Handles Opponent Turn
     * @param message Message
     */
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

    /**
     * Handles Game End
     * @param message Message
     */
    public void handleGameEnd(Message message) {
        boolean isWinner = message.getParameter(0).equals("YOU");

        model.clientState.resetExceptNickname();
        model.opponentState.resetExceptNickname();

        String alertContent = isWinner ? "You have won." : "You have lost.";
        stageManager.showAlertLater(Alert.AlertType.INFORMATION, "Game End", alertContent);
        stageManager.setSceneLater(StageManager.Scene.Room);
    }

    /**
     * Handles Opponent Rejoin
     * @param message Message
     */
    public void handleOpponentRejoin(Message message) {
        model.opponentState.isRespondingProperty().set(true);
    }

    /**
     * Handles Board State
     * @param message Message
     */
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

    /**
     * Handles Invalidate Field
     * @param message Message
     */
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
