import {
  View,
  Heading,
  Button,
  ButtonIcon,
  ButtonSpinner,
  ButtonText,
  Center,
  VStack,
  CloseIcon,
  Icon,
  ClockIcon,
  GlobeIcon,
  PlayIcon,
} from "@gluestack-ui/themed";
import Board from "./Board";
import { useState } from "react";
import { isBoardValidMove } from "./validateBoard";
import { useWindowDimensions } from "react-native";

/**
 * Stage for playing the game
 * @param props communicator (TCP socket message communicator), state (game state object), winner (game winner if any), onMove (handler on player turn), onRoomLeft (handler on room leave), onNewGame (handler on new game request)
 */
const Game = (props) => {
  const windowDimensions = useWindowDimensions(); // app viewport dimensions

  const [formState, setFormState] = useState({
    isDisabled: false,
    isLeaving: false,
  });

  const handleRoomLeave = () => {
    setFormState((old) => {
      return { ...old, isDisabled: true, isLeaving: true };
    });
    props.communicator.write("ROOM_LEAVE\n");
    props.communicator.wait("ACK", () => props.onRoomLeft());
  };

  const handleFieldClicked = (row, col) => {
    if (!props.state.player.isOnTurn || props.state.isTurning) {
      // validate the ability to turn
      return;
    }
    if (!isBoardValidMove(props.state.opponent.board, row, col)) {
      // validate the turn
      return;
    }

    props.onMove(row, col);
  };

  const hasOpponent = props.state.opponent.nickname != "";
  const inGame = props.state.player.isReady && props.state.opponent.isReady;
  return (
    <View flex p="$4">
      <View flexDirection="row" justifyContent="space-between">
        <Heading size="2xl">Room #{props.state.code}</Heading>
        {props.winner ? (
          <Button
            size="md"
            variant="solid"
            action="primary"
            onPress={() => props.onNewGame()}
          >
            <ButtonIcon as={PlayIcon} />
            <ButtonText> New Game</ButtonText>
          </Button>
        ) : (
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
        )}
      </View>
      <View my="$2" />
      <View flex>
        <Center flex>
          <VStack space="md" flex w="$full" justifyContent="space-around">
            <Center>
              <VStack space="xs">
                <View flexDirection="row" justifyContent="space-between">
                  <Heading size="sm">{props.state.player.nickname}</Heading>
                  {!inGame && !props.winner && (
                    <Heading size="sm">Ready</Heading>
                  )}
                  {inGame && props.state.player.isOnTurn && (
                    <View flexDirection="row" alignItems="center">
                      {!props.state.isTurning ? (
                        <Icon as={ClockIcon} />
                      ) : (
                        <Icon as={GlobeIcon} />
                      )}
                      <Heading size="sm">
                        {" "}
                        {!props.state.isTurning ? "On Turn" : "On Turn..."}
                      </Heading>
                    </View>
                  )}
                  {props.winner && (
                    <Heading size="sm">
                      {props.winner == "player" ? "WINNER" : "LOSER"}
                    </Heading>
                  )}
                </View>
                <View
                  w={
                    ((windowDimensions.width / 2.9) * windowDimensions.height) /
                    windowDimensions.width
                  }
                  maxWidth={384}
                >
                  <Board
                    board={props.state.player.board}
                    highlight={
                      props.state.lastTurn.player == "opponent"
                        ? [props.state.lastTurn.field]
                        : []
                    }
                  />
                </View>
              </VStack>
            </Center>
            <Center>
              <VStack space="xs">
                <View flexDirection="row" justifyContent="space-between">
                  {!hasOpponent ? (
                    <Heading size="sm">Waiting for the opponent...</Heading>
                  ) : (
                    <>
                      <Heading size="sm">
                        {props.state.opponent.nickname}
                      </Heading>
                      {!props.state.opponent.isReady && !props.winner && (
                        <Heading size="sm">Not Ready</Heading>
                      )}
                      {inGame && props.state.opponent.isOnTurn && (
                        <View flexDirection="row" alignItems="center">
                          <Icon as={ClockIcon} />
                          <Heading size="sm"> On Turn</Heading>
                        </View>
                      )}
                      {props.winner && (
                        <Heading size="sm">
                          {props.winner == "opponent" ? "WINNER" : "LOSER"}
                        </Heading>
                      )}
                    </>
                  )}
                </View>
                <View
                  w={
                    ((windowDimensions.width / 2.9) * windowDimensions.height) /
                    windowDimensions.width
                  }
                  maxWidth={384}
                >
                  <Board
                    board={props.state.opponent.board}
                    onFieldClicked={handleFieldClicked}
                    highlight={
                      props.state.lastTurn.player == "player"
                        ? [props.state.lastTurn.field]
                        : []
                    }
                  />
                </View>
              </VStack>
            </Center>
          </VStack>
        </Center>
      </View>
    </View>
  );
};

export default Game;
