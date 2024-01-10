package battleship.client.controllers;

import battleship.client.models.Model;
import battleship.client.views.StageManager;
import javafx.application.Platform;

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
    }

}
