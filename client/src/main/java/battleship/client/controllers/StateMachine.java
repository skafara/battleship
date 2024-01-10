package battleship.client.controllers;

import java.io.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class StateMachine implements Runnable {

    private final StateMachineController stateMachineController;
    private final BlockingDeque<Message> messages = new LinkedBlockingDeque<>();

    public StateMachine(StateMachineController stateMachineController) {
        this.stateMachineController = stateMachineController;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message msg = messages.take();
                System.out.println("Vyzvedavam zpravu " + msg.Serialize());

                Consumer<Message> handler;
                switch (msg.getType()) {
                    case CONN_TERM -> handler = stateMachineController::handleConnTerm;
                    case OPPONENT_NICKNAME_SET -> handler = stateMachineController::handleOpponentNicknameSet;
                    case OPPONENT_BOARD_READY -> handler = stateMachineController::handleOpponentBoardReady;
                    case OPPONENT_ROOM_LEAVE -> handler = stateMachineController::handleOpponentRoomLeave;
                    case GAME_BEGIN -> handler = stateMachineController::handleGameBegin;
                    case TURN_SET -> handler = stateMachineController::handleTurnSet;
                    case OPPONENT_NO_RESPONSE -> handler = stateMachineController::handleOpponentNoResponse;
                    case OPPONENT_TURN -> handler = stateMachineController::handleOpponentTurn;
                    case GAME_END -> handler = stateMachineController::handleGameEnd;
                    case OPPONENT_REJOIN -> handler = stateMachineController::handleOpponentRejoin;
                    case BOARD_STATE -> handler = stateMachineController::handleBoardState;
                    default -> {
                        System.out.println("nevyhovuje?");
                        continue;
                    }
                }
                handler.accept(msg);
            }
        }
        catch (InterruptedException e) {
            //
        }
    }

    public void enqueueMessage(Message message) {
        messages.add(message);
    }

}
