import {
  Button,
  ButtonText,
  Heading,
  View,
  CloseIcon,
  ButtonIcon,
  VStack,
  InfoIcon,
  Icon,
  CheckIcon,
  Center,
  ButtonSpinner,
  Text,
  AlertCircleIcon,
} from "@gluestack-ui/themed";
import Board from "../game/Board";
import { Linking, Pressable } from "react-native";
import { useState } from "react";
import FieldState from "../../util/FieldState";
import { isBoardValid } from "../../util/validateBoard";

/**
 * Stage for preparing the game board
 * @param props communicator (TCP socket message communicator), code (game room code), onRoomLeft (handler on room leave), onBoardReady (handler on ready board)
 */
const FormBoard = (props) => {
  const [formState, setFormState] = useState({
    isDisabled: false,
    isBoardValid: true,
    isLeaving: false,
    isReadying: false,
  });

  const [boardState, setBoardState] = useState(
    Array(100).fill(FieldState.NONE)
  );

  const handleRoomLeave = () => {
    setFormState((old) => {
      return { ...old, isDisabled: true, isLeaving: true };
    });
    props.communicator.write("ROOM_LEAVE\n");
    props.communicator.wait("ACK", () => props.onRoomLeft());
  };

  const handleReadySubmit = () => {
    if (!isBoardValid(boardState)) {
      setFormState((old) => {
        return { ...old, isBoardValid: false };
      });
      return;
    }

    setFormState((old) => {
      return { ...old, isDisabled: true, isReadying: true };
    });

    let msg = "BOARD_READY";
    // construct the message parameters
    for (let i = 0; i < boardState.length; i++) {
      if (boardState[i] == FieldState.SHIP) {
        const row = Math.floor(i / 10);
        const col = i % 10;
        msg += "|" + row + col;
      }
    }
    props.communicator.write(msg + "\n");
    props.communicator.wait("ACK", () => props.onBoardReady(boardState));
  };

  const handleFieldClicked = (row, col) => {
    setBoardState((old) => {
      let copy = [...old];
      if (copy[row * 10 + col] == FieldState.NONE) {
        copy[row * 10 + col] = FieldState.SHIP;
      } else {
        copy[row * 10 + col] = FieldState.NONE;
      }
      if (!formState.isBoardValid && isBoardValid(copy)) {
        // validate the move
        setFormState((old) => {
          return { ...old, isBoardValid: true };
        });
      }
      return copy;
    });
  };

  return (
    <View flex p="$4">
      <View flexDirection="row" justifyContent="space-between">
        <Heading size="2xl">Room #{props.code}</Heading>
        <Button
          size="md"
          variant="solid"
          action="negative"
          isDisabled={formState.isDisabled}
          onPress={handleRoomLeave}
        >
          {!formState.isLeaving ? (
            <>
              <ButtonIcon as={CloseIcon} />
              <ButtonText> Leave</ButtonText>
            </>
          ) : (
            <>
              <ButtonSpinner />
              <ButtonText> Leaving...</ButtonText>
            </>
          )}
        </Button>
      </View>
      <Center flex>
        <VStack space="2xl" maxWidth="$96">
          <VStack space="md">
            <Heading textAlign="center" size="md">
              Prepare the Board{" "}
              <Pressable
                onPress={() =>
                  Linking.openURL(
                    "https://en.wikipedia.org/wiki/Battleship_(game)"
                  )
                }
              >
                <Icon as={InfoIcon} />
              </Pressable>
            </Heading>
            <Board board={boardState} onFieldClicked={handleFieldClicked} />
            {!formState.isBoardValid && (
              <View
                flexDirection="row"
                justifyContent="center"
                alignItems="center"
              >
                <Icon as={AlertCircleIcon} color="$error700" />
                <Text color="$error700"> Illegal Board</Text>
              </View>
            )}
          </VStack>
          <Button
            size="md"
            variant="solid"
            action="positive"
            onPress={handleReadySubmit}
            isDisabled={formState.isDisabled}
          >
            {!formState.isReadying ? (
              <>
                <ButtonIcon as={CheckIcon} />
                <ButtonText> Ready</ButtonText>
              </>
            ) : (
              <>
                <ButtonSpinner />
                <ButtonText> Setting...</ButtonText>
              </>
            )}
          </Button>
        </VStack>
      </Center>
    </View>
  );
};

export default FormBoard;
