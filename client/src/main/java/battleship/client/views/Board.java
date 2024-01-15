package battleship.client.views;

import battleship.client.controllers.Controller;
import battleship.client.models.ApplicationState;
import battleship.client.models.BoardState;
import battleship.client.models.ClientState;
import battleship.client.models.Model;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.concurrent.CompletableFuture;

/**
 * Board
 */
public class Board extends GridPane {

    private static final int BOARD_SIZE = 10;
    private static final int CELL_SIZE = 24;
    private static final int STROKE_WIDTH = 1;
    private static final Color GRAY = Color.rgb(160, 160, 160);
    private static final Color YELLOW = Color.rgb(237, 234, 182);

    /**
     * Constructs a Board
     * @param model Model
     * @param controller Controller
     * @param isClient True if client's board, false if opponent's board
     * @param isInGame Game has begun and not ended yet
     */
    public Board(Model model, Controller controller, boolean isClient, BooleanBinding isInGame) {
        ClientState clientState = isClient ? model.clientState : model.opponentState;

        construct(model.applicationState, clientState, controller, isClient, isInGame);
        repaint(clientState.getBoardState());
    }

    private void construct(ApplicationState applicationState, ClientState clientState, Controller controller, boolean isClient, BooleanBinding isInGame) {
        StackPane empty = createCell();
        add(empty, 0, 0);
        for (int row = 0; row < BOARD_SIZE; row++) {
            StackPane cell = createCell();
            getText(cell).setText(String.format("%d", row + 1));
            add(cell, 0, row + 1);
        }
        for (int col = 0; col < BOARD_SIZE; col++) {
            StackPane cell = createCell();
            getText(cell).setText(String.format("%c", 'A' + col));
            add(cell, col + 1, 0);
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane cell = createCell();
                getText(cell).setFont(Font.font("Arial", FontWeight.BOLD, 2 * BOARD_SIZE));
                cell.setCursor(Cursor.HAND);
                add(cell, col + 1, row + 1);
            }
        }

        setAlignment(Pos.CENTER);
        setMaxSize((BOARD_SIZE + 1) * CELL_SIZE, (BOARD_SIZE + 1) * CELL_SIZE);

        if (isClient) {
            disableProperty().bind(Bindings.createBooleanBinding(() -> {
                return applicationState.roomDisableProperty().get() || clientState.isBoardReadyProperty().get();
            }, applicationState.roomDisableProperty(), clientState.isBoardReadyProperty()));
        }
        else {
            disableProperty().bind(Bindings.createBooleanBinding(() -> {
                    return applicationState.roomDisableProperty().get() ||
                           !isInGame.get() ||
                           clientState.isOnTurnProperty().get();
            }, applicationState.roomDisableProperty(), isInGame, clientState.isOnTurnProperty()));
        }

        clientState.getBoardState().getBoard().addListener((ListChangeListener<BoardState.Field>) change -> {
            repaint(clientState.getBoardState());
        });

        setOnMouseClicked(e -> handleClick(e, applicationState, clientState.getBoardState(), controller, isClient));
    }

    private static StackPane createCell() {
        StackPane stackPane = new StackPane();

        Rectangle rectangle = new Rectangle(CELL_SIZE, CELL_SIZE);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(STROKE_WIDTH);

        Text text = new Text();

        stackPane.getChildren().addAll(rectangle, text);
        return stackPane;
    }

    private StackPane getCell(int row, int col) {
        // 21 is for 1 empty cell and 2x 10 board fields annotation cells
        return (StackPane) getChildren().get(21 + BOARD_SIZE * row + col);
    }

    private static Rectangle getRectangle(StackPane stackPane) {
        return (Rectangle) stackPane.getChildren().get(0);
    }

    private static Text getText(StackPane stackPane) {
        return (Text) stackPane.getChildren().get(1);
    }

    private void repaint(BoardState boardState) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                BoardState.Field field = boardState.getField(row, col);
                StackPane cell = getCell(row, col);
                Rectangle rectangle = getRectangle(cell);
                Text text = getText(cell);

                switch (field) {
                    case NONE -> rectangle.setFill(Color.TRANSPARENT);
                    case SHIP -> rectangle.setFill(GRAY);
                    case HIT -> {
                        rectangle.setFill(GRAY);
                        rectangle.setStroke(Color.RED);
                        text.setText("X");
                        text.setFill(Color.RED);
                    }
                    case MISS, INVALIDATED -> {
                        rectangle.setFill(YELLOW);
                        text.setText("•");
                    }
                }
            }
        }
    }

    private void handleClick(MouseEvent mouseEvent, ApplicationState applicationState, BoardState boardState, Controller controller, boolean client) {
        int row = (int) (mouseEvent.getY() / (CELL_SIZE + STROKE_WIDTH) - 1);
        int col = (int) (mouseEvent.getX() / (CELL_SIZE + STROKE_WIDTH) - 1);

        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return;
        }

        if (client) {
            if (!boardState.isShip(row, col)) {
                boardState.setField(BoardState.Field.SHIP, row, col);
            } else {
                boardState.setField(BoardState.Field.NONE, row, col);
            }
        } else {
            handleTurn(row, col, applicationState, controller);
        }
    }

    private void handleTurn(int row, int col, ApplicationState applicationState, Controller controller) {
        applicationState.roomDisableProperty().set(true);

        CompletableFuture<Void> future = controller.turn(row, col);
        future.whenCompleteAsync((value, exception) -> {
            if (exception == null) {
                applicationState.roomDisableProperty().set(false);
                return;
            }

            StageManager stageManager = controller.getStageManager();
            try {
                throw exception;
            }
            catch (IllegalArgumentException e) {
                handleIllegalTurn(stageManager);
            }
            catch (Throwable e) {
                //
            }

            applicationState.roomDisableProperty().set(false);
        });
    }

    private static void handleIllegalTurn(StageManager stageManager) {
        stageManager.showAlertLater(Alert.AlertType.ERROR, "Invalid Turn", "Check the validity of your turn.");
    }

}
