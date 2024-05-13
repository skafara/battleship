import FieldState from "./FieldState";

const SIZE = 10;
const SHIPS_SIZES_CNTS = { 1: 4, 2: 3, 3: 2, 4: 1 };

const isBoardValid = (board) => {
  const shipsSizesCnts = { ...SHIPS_SIZES_CNTS };
  const isVisited = new Array(SIZE * SIZE).fill(false);

  for (let row = 0; row < SIZE; row++) {
    for (let col = 0; col < SIZE; col++) {
      if (
        Object.values(shipsSizesCnts).every((i) => i == 0) &&
        !isVisited[getFieldIndex(row, col)] &&
        isShip(board, row, col)
      ) {
        return false;
      }
      if (!isVisited[getFieldIndex(row, col)] && isShip(board, row, col)) {
        if (!processShip(board, row, col, isVisited, shipsSizesCnts)) {
          return false;
        }
      }
    }
  }
  return Object.values(shipsSizesCnts).every((i) => i == 0);
};

const processShip = (board, row, col, visited, shipsSizesCnts) => {
  // Check corners for diagonal overlapping
  if (row != 0 && col != 0 && isShip(board, row - 1, col - 1)) return false;
  if (row != 0 && col + 1 < SIZE && isShip(board, row - 1, col + 1))
    return false;
  if (row + 1 < SIZE && col != 0 && isShip(board, row + 1, col - 1))
    return false;
  if (row + 1 < SIZE && col + 1 < SIZE && isShip(board, row + 1, col + 1))
    return false;

  let offsetCol, offsetRow;
  // Check horizontal direction for ship continuation
  for (offsetCol = 1; ; offsetCol++) {
    if (col + offsetCol >= SIZE || !isShip(board, row, col + offsetCol)) break;

    // Check vertical direction for overlapping
    if (row + 1 < SIZE && isShip(board, row + 1, col + offsetCol)) return false;
    visited[getFieldIndex(row, col + offsetCol)] = true;
  }
  // Check vertical direction for ship continuation
  for (offsetRow = 1; ; offsetRow++) {
    if (row + offsetRow >= SIZE || !isShip(board, row + offsetRow, col)) break;

    // Check horizontal direction for overlapping
    if (col + 1 < SIZE && isShip(board, row + offsetRow, col + 1)) return false;
    visited[getFieldIndex(row + offsetRow, col)] = true;
  }

  if (offsetCol > 1 && offsetRow > 1) {
    return false;
  }

  const shipSize = Math.max(offsetCol, offsetRow);
  if (!shipsSizesCnts.hasOwnProperty(shipSize)) {
    return false;
  }

  shipsSizesCnts[shipSize] = shipsSizesCnts[shipSize] - 1;
  return true;
};

const getFieldIndex = (row, col) => {
  return SIZE * row + col;
};

const getField = (board, row, col) => {
  return board.at(getFieldIndex(row, col));
};

const isShip = (board, row, col) => {
  return getField(board, row, col) == FieldState.SHIP;
};

const isBoardValidMove = (board, row, col) => {
  const fieldState = getField(board, row, col);
  if (
    fieldState == FieldState.HIT ||
    fieldState == FieldState.MISS ||
    fieldState == FieldState.INVALIDATED
  ) {
    return false;
  }

  return true;
};

export { isBoardValid, isBoardValidMove };
