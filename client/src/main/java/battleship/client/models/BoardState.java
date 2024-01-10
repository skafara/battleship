package battleship.client.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BoardState {

    public enum Field {
        NONE,
        SHIP,
        HIT,
        MISS
    }

    public static final int SIZE = 10;

    private final ObservableList<Field> board = FXCollections.observableArrayList(Collections.nCopies(SIZE * SIZE, Field.NONE));

    public BoardState() {
        //
    }

    public boolean isValid() {
//std::map<size_t, size_t> ships_sizes_cnts{{1, 4}, {2, 3}, {3, 2}, {4, 1}};
        Map<Integer, Integer> shipsSizesCnts = new HashMap<>(Map.of(
                1, 2
        ));
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

    public Field getField(int row, int col) {
        return board.get(getFieldIndex(row, col));
    }

    public boolean isShip(int row, int col) {
        return getField(row, col) == Field.SHIP;
    }

    public boolean isGuess(int row, int col) {
        Field field = getField(row, col);
        return field == Field.HIT || field == Field.MISS;
    }

    public void setField(Field field, int row, int col) {
        board.set(getFieldIndex(row, col), field);
    }

    public void setField(Field field, int fieldIndex) {
        board.set(fieldIndex, field);
    }

    public ObservableList<Field> getBoard() {
        return board;
    }

    public void reset() {
        Collections.fill(board, Field.NONE);
    }

    private int getFieldIndex(int row, int col) {
        return SIZE * row + col;
    }

    public static String SerializeField(int row, int col) {
        return String.format("%d%d", row, col);
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
