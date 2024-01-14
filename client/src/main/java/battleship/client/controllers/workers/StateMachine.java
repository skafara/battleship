package battleship.client.controllers.workers;

import battleship.client.controllers.StateMachineController;
import battleship.client.controllers.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class StateMachine implements Runnable {

    private final Logger logger = LogManager.getLogger();

    private final StateMachineController stateMachineController;
    private final BlockingDeque<Message> messages = new LinkedBlockingDeque<>();

    public StateMachine(StateMachineController stateMachineController) {
        this.stateMachineController = stateMachineController;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message message = messages.take();
                logger.trace("Take Message: " + message.serialize());

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }


                Consumer<Message> handler;
                switch (message.getType()) {
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
                    case INVALIDATE_FIELD -> handler = stateMachineController::handleInvalidateField;
                    default -> {
                        logger.trace("No Suitable Handler");
                        continue;
                    }
                }
                logger.trace("Handle Message");
                handler.accept(message);
            }
        }
        catch (InterruptedException e) {
            logger.debug("Interrupted");
        }
    }

    public void enqueueMessage(Message message) {
        logger.trace("Enqueue Message: " + message.serialize());
        messages.add(message);
    }

}
