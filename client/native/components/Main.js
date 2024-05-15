import {
  Modal,
  ModalBackdrop,
  ModalContent,
  ModalBody,
  Text,
  ModalHeader,
  Heading,
  ModalFooter,
  Button,
  ButtonText,
} from "@gluestack-ui/themed";
import { useRef, useState } from "react";
import { Audio } from "expo-av";
import FormConnect from "./menu/FormConnect";
import FormLobby from "./menu/FormLobby";
import FormBoard from "./game/FormBoard";
import Game from "./game/Game";
import FieldState from "./game/FieldState";

/**
 * Application stages (screens)
 */
const Stages = Object.freeze({
  CONNECT: "connect",
  LOBBY: "lobby",
  BOARD: "board",
  GAME: "game",
});

const Main = () => {
  /** Active stage */
  const [stage, setStage] = useState(Stages.CONNECT);
  /** Object holding the state of the application and game */
  const [gameState, setGameState] = useState({
    code: "",
    isTurning: false,
    lastTurn: { player: "", field: { row: 0, col: 0 } },
    winner: "",
    player: {
      isReady: false,
      isOnTurn: false,
      board: [],
      nickname: "",
    },
    opponent: {
      isReady: false,
      isOnTurn: false,
      board: [],
      nickname: "",
    },
  });

  const [showOpponentRoomLeaveModal, setShowOpponentRoomLeaveModal] =
    useState(false);

  const [showConnectionTerminatedModal, setShowConnectionTerminatedModal] =
    useState(false);

  /** Ref to TCP socket message communicator */
  const communicatorRef = useRef();
  const [formConnectState, setFormConnectState] = useState();

  /** Handles established connection to the server
   * Sets up server message handlers and updates the stage
   * @param {Communicator} communicator - TCP socket message communicator
   * @param {Object} values - Form values
   */
  const handleConnectionEstablished = (communicator, values) => {
    communicatorRef.current = communicator;
    setGameState((old) => {
      return { ...old, player: { ...old.player, nickname: values.nickname } };
    });
    setStage(Stages.LOBBY);
    setHandlers(communicator);
    setFormConnectState(values);
  };

  const handleRejoin = (communicator, values, params) => {
    communicatorRef.current = communicator;
    setGameState((old) => {
      return {
        ...old,
        code: params[1],
        player: { ...old.player, nickname: values.nickname },
      };
    });
    setHandlers(communicator);
    if (params[0] == "ROOM") {
      setStage(Stages.BOARD);
    } else {
      setStage(Stages.GAME);
    }
    setFormConnectState(values);
  };

  const setHandlers = (communicator) => {
    communicator.on("OPPONENT_NICKNAME_SET", handleOpponnentNicknameSet);
    communicator.on("OPPONENT_ROOM_LEAVE", handleOpponnentRoomLeave);
    communicator.on("OPPONENT_BOARD_READY", handleOpponentBoardReady);
    communicator.on("BOARD_STATE", handleBoardState);
    communicator.on("TURN_SET", handleTurnSet);
    communicator.on("INVALIDATE_FIELD", handleInvalidateField);
    communicator.on("OPPONENT_TURN", handleOpponentTurn);
    communicator.on("GAME_END", handleGameEnd);

    const interval = setInterval(
      // Send keep alive message every 5 seconds
      () => communicator.write("KEEP_ALIVE\n"),
      5000
    );
    communicator.client.on("close", () => {
      // Stop sending keep alive messages on connection close (server termination, error)
      clearInterval(interval);
      setStage(Stages.CONNECT);
      setShowConnectionTerminatedModal(true);
    });
  };

  const handleRoomJoined = (code) => {
    setGameState((old) => {
      return {
        ...old,
        code: code,
        opponent: {
          ...old.opponent,
          isReady: false,
          isOnTurn: false,
          board: new Array(100).fill(FieldState.NONE),
          nickname: "",
        },
      };
    });
    setStage(Stages.BOARD);
  };

  const handleRoomLeft = () => {
    setStage(Stages.LOBBY);
  };

  const handleNewGame = () => {
    setGameState((old) => {
      return {
        ...old,
        winner: "",
        lastTurn: { player: "", field: { row: 0, col: 0 } },
        player: {
          ...old.player,
          isReady: false,
          board: new Array(100).fill(FieldState.NONE),
        },
        opponent: {
          ...old.opponent,
          board: new Array(100).fill(FieldState.NONE),
        },
      };
    });
    setStage(Stages.BOARD);
  };

  const handleBoardReady = (board) => {
    setGameState((old) => {
      return {
        ...old,
        winner: "",
        lastTurn: { player: "", field: { row: 0, col: 0 } },
        player: { ...old.player, isReady: true, board: board },
        opponent: {
          ...old.opponent,
          board: new Array(100).fill(FieldState.NONE),
        },
      };
    });
    setStage(Stages.GAME);
  };

  const handleOpponnentNicknameSet = (params) => {
    setGameState((old) => {
      return { ...old, opponent: { ...old.opponent, nickname: params[0] } };
    });
  };

  const handleOpponnentRoomLeave = (params) => {
    setShowOpponentRoomLeaveModal(true);
    setStage(Stages.LOBBY);
  };

  const handleOpponentBoardReady = (params) => {
    setGameState((old) => {
      return { ...old, opponent: { ...old.opponent, isReady: true } };
    });
  };

  const handleBoardState = (params) => {
    let board = new Array(100);
    for (let i = 0; i < board.length; i++) {
      const fieldState = params.at(i + 1);
      if (fieldState == "NONE") {
        board[i] = FieldState.NONE;
      } else if (fieldState == "SHIP") {
        board[i] = FieldState.SHIP;
      } else if (fieldState == "HIT") {
        board[i] = FieldState.HIT;
      } else if (fieldState == "MISS") {
        board[i] = FieldState.MISS;
      } else if (fieldState == "INVALIDATED") {
        board[i] = FieldState.INVALIDATED;
      }
    }
    if (params[0] == "YOU") {
      setGameState((old) => {
        return {
          ...old,
          player: { ...old.player, board: board, isReady: true },
        };
      });
      if (stage != Stages.GAME) {
        setStage(Stages.GAME);
      }
    } else {
      setGameState((old) => {
        return {
          ...old,
          opponent: { ...old.opponent, board: board, isReady: true },
        };
      });
    }
  };

  const handleTurnSet = (params) => {
    if (params[0] == "YOU") {
      setGameState((old) => {
        return {
          ...old,
          player: { ...old.player, isOnTurn: true },
          opponent: { ...old.opponent, isOnTurn: false },
        };
      });
    } else {
      setGameState((old) => {
        return {
          ...old,
          player: { ...old.player, isOnTurn: false },
          opponent: { ...old.opponent, isOnTurn: true },
        };
      });
    }
  };

  const handleInvalidateField = (params) => {
    const field = parseInt(params[1]);
    const row = Math.floor(field / 10);
    const col = field % 10;
    setGameState((old) => {
      let copy;
      if (params[0] == "YOU") {
        copy = [...old.player.board];
        copy[row * 10 + col] = FieldState.INVALIDATED;
        return { ...old, player: { ...old.player, board: copy } };
      } else {
        copy = [...old.opponent.board];
        copy[row * 10 + col] = FieldState.INVALIDATED;
        return { ...old, opponent: { ...old.opponent, board: copy } };
      }
    });
  };

  const handleOpponentTurn = async (params) => {
    const field = parseInt(params[0]);
    const row = Math.floor(field / 10);
    const col = field % 10;
    setGameState((old) => {
      let copy = [...old.player.board];
      if (params[1] == "HIT") {
        copy[row * 10 + col] = FieldState.HIT;
      } else {
        copy[row * 10 + col] = FieldState.MISS;
      }
      return {
        ...old,
        lastTurn: { player: "opponent", field: { row: row, col: col } },
        player: { ...old.player, board: copy },
      };
    });
    const audio = new Audio.Sound();
    if (params[1] == "HIT") {
      await audio.loadAsync(require("../assets/audio/explosion.mp3"));
    } else {
      await audio.loadAsync(require("../assets/audio/plop.mp3"));
    }
    await audio.playAsync();
  };

  const handleGameEnd = async (params) => {
    setGameState((old) => {
      return {
        ...old,
        winner: params[0] == "YOU" ? "player" : "opponent",
        player: { ...old.player, isOnTurn: false, isReady: false },
        opponent: { ...old.opponent, isOnTurn: false, isReady: false },
      };
    });
    const audio = new Audio.Sound();
    if (params[0] == "YOU") {
      await audio.loadAsync(require("../assets/audio/win.mp3"));
    } else {
      await audio.loadAsync(require("../assets/audio/lose.mp3"));
    }
    await audio.playAsync();
  };

  /**
   * Handles player turn on field - sends TURN message to the server and waits for TURN_RESULT,
   * based on the result updates the game state
   * @param row Board row
   * @param col Board column
   */
  const handleMove = (row, col) => {
    setGameState((old) => {
      return { ...old, isTurning: true };
    });
    const communicator = communicatorRef.current;
    communicator.write("TURN|" + row + col + "\n");
    communicator.wait("TURN_RESULT", async (params) => {
      setGameState((old) => {
        let copy = [...old.opponent.board];
        if (params[1] == "HIT") {
          copy[row * 10 + col] = FieldState.HIT;
        } else {
          copy[row * 10 + col] = FieldState.MISS;
        }
        return {
          ...old,
          isTurning: false,
          lastTurn: { player: "player", field: { row: row, col: col } },
          opponent: { ...old.opponent, board: copy },
        };
      });
      const audio = new Audio.Sound();
      if (params[1] == "HIT") {
        await audio.loadAsync(require("../assets/audio/explosion.mp3"));
      } else {
        await audio.loadAsync(require("../assets/audio/plop.mp3"));
      }
      await audio.playAsync();
    });
  };

  if (stage == Stages.CONNECT) {
    return (
      <>
        {!formConnectState ? (
          <FormConnect
            onConnectionEstablished={handleConnectionEstablished}
            onRejoin={handleRejoin}
          />
        ) : (
          <FormConnect
            onConnectionEstablished={handleConnectionEstablished}
            onRejoin={handleRejoin}
            values={formConnectState}
          />
        )}

        <Modal isOpen={showConnectionTerminatedModal} size="md">
          <ModalBackdrop />
          <ModalContent>
            <ModalHeader>
              <Heading size="xl">Connection Terminated</Heading>
            </ModalHeader>
            <ModalBody>
              <Text>
                The server has terminated the connection. Please reconnect.
              </Text>
            </ModalBody>
            <ModalFooter>
              <Button
                variant="solid"
                size="sm"
                action="secondary"
                onPress={() => {
                  setShowConnectionTerminatedModal(false);
                }}
              >
                <ButtonText>OK</ButtonText>
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>
      </>
    );
  } else if (stage == Stages.LOBBY) {
    return (
      <>
        <FormLobby
          communicator={communicatorRef.current}
          onRoomJoined={handleRoomJoined}
        />

        <Modal isOpen={showOpponentRoomLeaveModal} size="md">
          <ModalBackdrop />
          <ModalContent>
            <ModalHeader>
              <Heading size="xl">Room Canceled</Heading>
            </ModalHeader>
            <ModalBody>
              <Text>
                Your opponent has left and the room has been canceled.
              </Text>
            </ModalBody>
            <ModalFooter>
              <Button
                variant="solid"
                size="sm"
                action="secondary"
                onPress={() => {
                  setShowOpponentRoomLeaveModal(false);
                }}
              >
                <ButtonText>OK</ButtonText>
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>
      </>
    );
  } else if (stage == Stages.BOARD) {
    return (
      <FormBoard
        communicator={communicatorRef.current}
        code={gameState.code}
        onRoomLeft={handleRoomLeft}
        onBoardReady={handleBoardReady}
      />
    );
  } else {
    if (gameState.winner == "") {
      return (
        <Game
          communicator={communicatorRef.current}
          state={gameState}
          onRoomLeft={handleRoomLeft}
          onNewGame={handleNewGame}
          onMove={handleMove}
        />
      );
    } else {
      return (
        <Game
          communicator={communicatorRef.current}
          state={gameState}
          onRoomLeft={handleRoomLeft}
          onNewGame={handleNewGame}
          onMove={handleMove}
          winner={gameState.winner}
        />
      );
    }
  }
};

export default Main;
