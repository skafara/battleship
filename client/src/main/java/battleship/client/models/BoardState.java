package battleship.client.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Board state
 */
public class BoardState {

    /** Field */
    public enum Field {
        NONE,
        SHIP,
        HIT,
        MISS,
        INVALIDATED
    }

    /** Board Size */
    public static final int SIZE = 10;

    private static final boolean IS_DEBUG = false;
    private static final Map<Integer, Integer> SHIPS_SIZES_CNTS = IS_DEBUG ? Map.of(
            1, 2
    ) : Map.of(
            1, 4,
            2, 3,
            3, 2,
            4, 1
    );

    private final ObservableList<Field> board = FXCollections.observableArrayList(Collections.nCopies(SIZE * SIZE, Field.NONE));

    /**
     * Constructs a Board State
     */
    public BoardState() {
        //
    }

    /**
     * Returns whether a board is valid (valid ships placement)
     * @return Boolean
     */
    public boolean isValid() {
        Map<Integer, Integer> shipsSizesCnts = new HashMap<>(SHIPS_SIZES_CNTS);
        boolean[] isVisited = new boolean[SIZE * SIZE];

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (shipsSizesCnts.values().stream().allMatch(i -> i == 0) &&
                    !isVisited[getFieldIndex(row, col)] && isShip(row, col))
                {
                    return false;
                }
                if (!isVisited[getFieldIndex(row, col)] && isShip(row, col)) {
                    if (!processShip(row, col, isVisited, shipsSizesCnts)) {
                        return false;
                    }
                }
            }
        }

        return shipsSizesCnts.values().stream().allMatch(i -> i == 0);
    }

    /**
     * Returns field on (row, col)
     * @param row Row
     * @param col Col
     * @return Field
     */
    public Field getField(int row, int col) {
        return board.get(getFieldIndex(row, col));
    }

    /**
     * Returns whether there is a ship on (row, col)
     * @param row Row
     * @param col Col
     * @return Boolean
     */
    public boolean isShip(int row, int col) {
        return getField(row, col) == Field.SHIP;
    }

    /**
     * Returns whether (row, col) has been previously guessed
     * @param row Row
     * @param col Col
     * @return Boolean
     */
    public boolean isGuess(int row, int col) {
        Field field = getField(row, col);
        return field == Field.HIT || field == Field.MISS;
    }

    /**
     * Returns whether (row, col) has been previously invalidated (not-guessed neighbour fields left after a ship is sunk)
     * @param row Row
     * @param col Col
     * @return Boolean
     */
    public boolean isInvalidated(int row, int col) {
        return getField(row, col) == Field.INVALIDATED;
    }

    /**
     * Sets field on (row, col)
     * @param field Field
     * @param row Row
     * @param col Col
     */
    public void setField(Field field, int row, int col) {
        board.set(getFieldIndex(row, col), field);
    }

    /**
     * Sets fieldIndex'th field
     * @param field Field
     * @param fieldIndex Index
     */
    public void setField(Field field, int fieldIndex) {
        board.set(fieldIndex, field);
    }

    /**
     * Returns observable list of fields
     * @return Observable list of fields
     */
    public ObservableList<Field> getBoard() {
        return board;
    }

    /**
     * Resets board state
     */
    public void reset() {
        Collections.fill(board, Field.NONE);
    }

    /**
     * Serializes a field position
     * @param row Row
     * @param col Col
     * @return String
     */
    public static String SerializeField(int row, int col) {
        return String.format("%d%d", row, col);
    }

    private static int getFieldIndex(int row, int col) {
        return SIZE * row + col;
    }

    private boolean processShip(int row, int col, boolean[] visited, Map<Integer, Integer> shipsSizesCnts) {
        // Check corners for diagonal overlapping
        if (row != 0 && col != 0 && isShip(row - 1, col - 1)) return false;
        if (row != 0 && col + 1 < SIZE && isShip(row - 1, col + 1)) return false;
        if (row + 1 < SIZE && col != 0 && isShip(row + 1, col - 1)) return false;
        if (row + 1 < SIZE && col + 1 < SIZE && isShip(row + 1, col + 1)) return false;

        int offsetCol, offsetRow;
        // Check horizontal direction for ship continuation
        for (offsetCol = 1; ; offsetCol++) {
            if (col + offsetCol >= SIZE || !isShip(row, col + offsetCol)) break;

            // Check vertical direction for overlapping
            if (row + 1 < SIZE && isShip(row + 1, col + offsetCol)) return false;
            visited[getFieldIndex(row, col + offsetCol)] = true;
        }
        // Check vertical direction for ship continuation
        for (offsetRow = 1; ; offsetRow++) {
            if (row + offsetRow >= SIZE || !isShip(row + offsetRow, col)) break;

            // Check horizontal direction for overlapping
            if (col + 1 < SIZE && isShip(row + offsetRow, col + 1)) return false;
            visited[getFieldIndex(row + offsetRow, col)] = true;
        }

        if (offsetCol > 1 && offsetRow > 1) {
            return false;
        }

        int shipSize = Math.max(offsetCol, offsetRow);
        if (!shipsSizesCnts.containsKey(shipSize)) {
            return false;
        }

        shipsSizesCnts.put(shipSize, shipsSizesCnts.get(shipSize) - 1);
        return true;
    }

}
