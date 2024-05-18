import { View, Pressable, Text, Center } from "@gluestack-ui/themed";
import FieldState from "../../util/FieldState";

/**
 * Represents the game board
 * @param props board (game board to visualise), onFieldClicked (handler on field click), highlight (highlighted fields)
 */
const Board = (props) => {
  const handleClick = (row, col) => {
    if (row == 0 || col == 0) {
      return;
    }

    if (props.onFieldClicked) props.onFieldClicked(row - 1, col - 1);
  };

  const render = () => {
    let cells = [];
    for (let row = 0; row < 11; row++) {
      for (let col = 0; col < 11; col++) {
        let text;
        if (col == 0 && row > 0) {
          text = row;
        } else if (row == 0 && col > 0) {
          text = String.fromCharCode(64 + col);
        } else {
          switch (props.board[(row - 1) * 10 + (col - 1)]) {
            case FieldState.HIT:
              text = "X";
              break;
            case FieldState.MISS:
            case FieldState.INVALIDATED:
              text = "•";
          }
        }

        let bgColor;
        let color;
        if (row > 0 && col > 0) {
          switch (props.board[(row - 1) * 10 + col - 1]) {
            case FieldState.SHIP:
            case FieldState.HIT:
              bgColor = "$light500";
              color = "$error700";
              break;
            case FieldState.INVALIDATED:
            case FieldState.MISS:
              bgColor = "$amber200";
              break;
          }
        }

        const isHighlighted =
          props.highlight &&
          props.highlight.some((obj) => {
            return Object.keys({ row: row - 1, col: col - 1 }).every((key) => {
              return obj[key] == { row: row - 1, col: col - 1 }[key];
            });
          });
        const cell = (
          <Pressable
            onPress={() => handleClick(row, col)}
            borderColor={!isHighlighted ? "$black" : "$warning500"}
            borderWidth={!isHighlighted ? "$1" : "$2"}
            w="9.09%"
            h="$0"
            aspectRatio={1}
            key={`${row},${col}`}
          >
            <Center flex bg={bgColor}>
              <Text fontWeight="$bold" color={color}>
                {text}
              </Text>
            </Center>
          </Pressable>
        );
        cells.push(cell);
      }
    }
    return cells;
  };

  return (
    <View flexDirection="row" flexWrap="wrap">
      {render()}
    </View>
  );
};

export default Board;
