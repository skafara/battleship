import {
  KeyboardAvoidingView,
  Center,
  VStack,
  Heading,
  Box,
  FormControl,
  FormControlLabel,
  FormControlLabelText,
  Input,
  InputField,
  FormControlError,
  FormControlErrorText,
  FormControlErrorIcon,
  AlertCircleIcon,
  Button,
  ButtonText,
  ButtonSpinner,
  AddIcon,
  ButtonIcon,
  LinkIcon,
} from "@gluestack-ui/themed";
import { useRef, useState } from "react";

const FORM_TEXTS = {
  form: {
    isRequired: "Required Field",
  },
  code: {
    isInvalid: "Invalid Code",
    notExists: "Not Existing Code",
  },
  buttonJoin: {
    join: "Join",
    joining: "Joining...",
  },
  buttonCreate: {
    create: "Create",
    creating: "Creating...",
  },
};

const isValidCode = (code) => {
  if (code.length != 4) {
    return false;
  }

  return Number.isInteger(parseInt(code));
};

const FormLobby = (props) => {
  const [formState, setFormState] = useState({
    form: {
      isDisabled: false,
      isJoining: false,
      isCreating: false,
    },
    code: {
      isInvalid: false,
    },
    buttonJoin: {
      text: FORM_TEXTS.buttonJoin.join,
    },
    buttonCreate: {
      text: FORM_TEXTS.buttonCreate.create,
    },
  });

  const inputCodeRef = useRef("7938");

  const handleCodeInputTextChange = (text) => {
    inputCodeRef.current = text;
  };

  const handleRoomNotExists = () => {
    console.log("aaa");
    setFormState((old) => {
      return {
        ...old,
        form: {
          ...old.form,
          isDisabled: false,
          isJoining: false,
        },
        code: {
          ...old.code,
          isInvalid: true,
          errorText: FORM_TEXTS.code.notExists,
        },
        buttonJoin: {
          ...old.buttonJoin,
          text: FORM_TEXTS.buttonJoin.join,
        },
      };
    });
  };

  const handleFormJoinSubmit = () => {
    if (!inputCodeRef.current) {
      setFormState((old) => {
        return {
          ...old,
          code: {
            ...old.code,
            isInvalid: true,
            errorText: FORM_TEXTS.form.isRequired,
          },
        };
      });
      return;
    }
    if (!isValidCode(inputCodeRef.current)) {
      setFormState((old) => {
        return {
          ...old,
          code: {
            ...old.code,
            isInvalid: true,
            errorText: FORM_TEXTS.code.isInvalid,
          },
        };
      });
      return;
    }

    setFormState((old) => {
      return {
        ...old,
        form: { ...old.form, isDisabled: true, isJoining: true },
        code: { ...old.code, isInvalid: false },
        buttonJoin: {
          ...old.buttonJoin,
          text: FORM_TEXTS.buttonJoin.joining,
        },
      };
    });

    const code = inputCodeRef.current;
    props.communicator.write(`ROOM_JOIN|${code}\n`);
    props.communicator.wait("ROOM_NOT_EXISTS", (params) =>
      handleRoomNotExists()
    );
    props.communicator.wait("ACK", (params) => props.onRoomJoined(code));
  };

  const handleFormCreateSubmit = () => {
    setFormState((old) => {
      return {
        ...old,
        form: { ...old.form, isDisabled: true, isCreating: true },
        code: { ...old.code, isInvalid: false },
        buttonCreate: {
          ...old.buttonCreate,
          text: FORM_TEXTS.buttonCreate.creating,
        },
      };
    });

    props.communicator.write("ROOM_CREATE\n");
    props.communicator.wait("ROOM_CREATED", (params) =>
      props.onRoomJoined(params[0])
    );
  };

  return (
    <KeyboardAvoidingView behavior="padding" flex>
      <Center flex>
        <VStack space="4xl">
          <VStack space="lg">
            <Heading size="2xl">Join Room</Heading>
            <Box w="$64">
              <VStack space="md">
                <FormControl
                  size="md"
                  isDisabled={formState.form.isDisabled}
                  isInvalid={formState.code.isInvalid}
                  isRequired={true}
                >
                  <FormControlLabel mb="$1">
                    <FormControlLabelText>Code</FormControlLabelText>
                  </FormControlLabel>
                  <Input>
                    <InputField
                      type="text"
                      keyboardType="numeric"
                      onChangeText={(text) => handleCodeInputTextChange(text)}
                    />
                  </Input>
                  <FormControlError>
                    <FormControlErrorIcon as={AlertCircleIcon} />
                    <FormControlErrorText>
                      {formState.code.errorText}
                    </FormControlErrorText>
                  </FormControlError>
                </FormControl>

                {/* Button */}
                <Button
                  size="md"
                  variant="solid"
                  action="primary"
                  isDisabled={formState.form.isDisabled}
                  onPress={handleFormJoinSubmit}
                >
                  {!formState.form.isJoining ? (
                    <ButtonIcon as={LinkIcon} />
                  ) : (
                    <ButtonSpinner />
                  )}
                  <ButtonText> {formState.buttonJoin.text}</ButtonText>
                </Button>
              </VStack>
            </Box>
          </VStack>
          <VStack space="lg">
            <Heading size="2xl">Create Room</Heading>
            <Box w="$64">
              {/* Button */}
              <Button
                size="md"
                variant="solid"
                action="primary"
                isDisabled={formState.form.isDisabled}
                onPress={handleFormCreateSubmit}
              >
                {!formState.form.isCreating ? (
                  <ButtonIcon as={AddIcon} />
                ) : (
                  <ButtonSpinner />
                )}
                <ButtonText> {formState.buttonCreate.text}</ButtonText>
              </Button>
            </Box>
          </VStack>
        </VStack>
      </Center>
    </KeyboardAvoidingView>
  );
};

export default FormLobby;
