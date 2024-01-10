package battleship.client.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;

public class BoardState {

    public enum Field {
        None,
        Ship,
        Hit,
        Miss
    }

    private static final int SIZE = 10;

    private final ObservableList<Field> board = FXCollections.observableArrayList(Collections.nCopies(SIZE * SIZE, Field.None));

    public BoardState() {
        //
    }

    public Field getField(int row, int col) {
        return board.get(getFieldIndex(row, col));
    }

    public void setField(Field field, int row, int col) {
        board.set(getFieldIndex(row, col), field);
    }

    public ObservableList<Field> getBoard() {
        return board;
    }

    public void reset() {
        Collections.fill(board, Field.None);
    }

    private int getFieldIndex(int row, int col) {
        return SIZE * row + col;
    }

}
