import {
  Center,
  Box,
  FormControl,
  FormControlLabel,
  FormControlLabelText,
  FormControlError,
  FormControlErrorIcon,
  FormControlErrorText,
  FormControlHelper,
  FormControlHelperText,
  Button,
  ButtonText,
  ButtonIcon,
  Input,
  InputField,
  AlertCircleIcon,
  VStack,
  KeyboardAvoidingView,
  Heading,
  ButtonSpinner,
  Modal,
  ModalBackdrop,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  Text,
  GlobeIcon,
} from "@gluestack-ui/themed";
import { useRef, useState } from "react";
import TcpSocket from "react-native-tcp-socket";
import Communicator from "../../util/Communicator";

const FORM_TEXTS = {
  form: {
    isRequired: "Required Field",
  },
  port: {
    isInvalid: "Invalid Port",
  },
  nickname: {
    exists: "Nickname Exists",
  },
  button: {
    connect: "Connect",
    connecting: "Connecting...",
  },
};

const isValidPort = (port) => {
  const portNumber = parseInt(port);
  return Number.isInteger(portNumber) && portNumber >= 0 && portNumber <= 65535;
};

/**
 * Stage for connecting to the server
 * @param {Object} props - onConnectionEstablished (handler on normally established connection), onRejoin (handler on established connection with prior disconnect), values (form input values)
 */
const FormConnect = (props) => {
  const [formState, setFormState] = useState({
    form: {
      isDisabled: false,
    },
    address: {
      isInvalid: false,
    },
    port: {
      isInvalid: false,
    },
    nickname: {
      isInvalid: false,
    },
    button: {
      text: FORM_TEXTS.button.connect,
    },
  });

  const [showErrorModal, setShowErrorModal] = useState();

  /** Form input values */
  const formInputsRef = useRef({
    address: "",
    port: "",
    nickname: "",
  });

  if (props.values) {
    // If values are passed, set the form inputs, otherwise keep them empty
    formInputsRef.current.address = props.values.address;
    formInputsRef.current.port = props.values.port;
    formInputsRef.current.nickname = props.values.nickname;
  }

  const handleFormSubmit = () => {
    let isInvalid = false;
    for (let input in formInputsRef.current) {
      const inputText = formInputsRef.current[input];
      if (!inputText) {
        isInvalid = true;
        setFormState((old) => {
          return {
            ...old,
            [input]: {
              ...old[input],
              isInvalid: true,
              errorText: FORM_TEXTS.form.isRequired,
            },
          };
        });
      } else {
        setFormState((old) => {
          return {
            ...old,
            [input]: {
              ...old[input],
              isInvalid: false,
            },
          };
        });
      }
    }
    if (isInvalid) return;

    const inputAddress = formInputsRef.current.address;
    const inputPort = formInputsRef.current.port;
    const inputNickname = formInputsRef.current.nickname;
    if (!isValidPort(inputPort)) {
      setFormState((old) => {
        return {
          ...old,
          port: {
            ...old.port,
            isInvalid: true,
            errorText: FORM_TEXTS.port.isInvalid,
          },
        };
      });
      return;
    }

    setFormState((old) => {
      return {
        ...old,
        form: { ...old.form, isDisabled: true },
        button: { ...old.button, text: FORM_TEXTS.button.connecting },
      };
    });

    const client = TcpSocket.createConnection({
      host: inputAddress,
      port: inputPort,
    });

    const formValues = {
      address: inputAddress,
      port: inputPort,
      nickname: inputNickname,
    };

    client.on("connect", () => {
      const communicator = new Communicator(client);
      communicator.write(`NICKNAME_SET|${inputNickname}\n`); // Try to set the nickname
      communicator.wait("ACK", () =>
        // Nickname not taken, set successfully, connection established
        props.onConnectionEstablished(communicator, formValues)
      );
      communicator.wait("NICKNAME_EXISTS", () => {
        // Nickname already taken (active)
        setFormState((old) => {
          return {
            ...old,
            form: {
              ...old.form,
              isDisabled: false,
            },
            nickname: {
              ...old.nickname,
              isInvalid: true,
              errorText: FORM_TEXTS.nickname.exists,
            },
            button: { ...old.button, text: FORM_TEXTS.button.connect },
          };
        });
        client.destroy();
      });
      communicator.wait(
        "REJOIN",
        (
          params // Nickname already taken (inactive for an acceptable time period), rejoin, restore game state
        ) => props.onRejoin(communicator, formValues, params)
      );
    });

    client.on("timeout", () => {
      // On connection timeout
      setShowErrorModal(true);
      setFormState((old) => {
        return {
          ...old,
          form: { ...old.form, isDisabled: false },
          button: { ...old.button, text: FORM_TEXTS.button.connect },
        };
      });
    });

    client.on("error", () => {
      // On connection error
      setShowErrorModal(true);
      setFormState((old) => {
        return {
          ...old,
          form: { ...old.form, isDisabled: false },
          button: { ...old.button, text: FORM_TEXTS.button.connect },
        };
      });
    });
  };

  const handleInputTextChange = (formInput, text) => {
    formInputsRef.current[formInput] = text;
  };

  return (
    <>
      <KeyboardAvoidingView behavior="padding" flex>
        <Center flex>
          <VStack space="4xl">
            <Heading size="2xl">Join Server</Heading>
            <Box w="$64">
              <VStack space="2xl">
                <VStack space="md">
                  {/* Address */}
                  <FormControl
                    size="md"
                    isDisabled={formState.form.isDisabled}
                    isInvalid={formState.address.isInvalid}
                    isRequired={true}
                  >
                    <FormControlLabel mb="$1">
                      <FormControlLabelText>Address</FormControlLabelText>
                    </FormControlLabel>
                    <Input>
                      <InputField
                        type="text"
                        onChangeText={(text) =>
                          handleInputTextChange("address", text)
                        }
                      >
                        {formInputsRef.current.address}
                      </InputField>
                    </Input>
                    <FormControlHelper>
                      <FormControlHelperText>
                        Hostname / IP Address
                      </FormControlHelperText>
                    </FormControlHelper>
                    <FormControlError>
                      <FormControlErrorIcon as={AlertCircleIcon} />
                      <FormControlErrorText>
                        {formState.address.errorText}
                      </FormControlErrorText>
                    </FormControlError>
                  </FormControl>

                  {/* Port */}
                  <FormControl
                    size="md"
                    isDisabled={formState.form.isDisabled}
                    isInvalid={formState.port.isInvalid}
                    isRequired={true}
                  >
                    <FormControlLabel mb="$1">
                      <FormControlLabelText>Port</FormControlLabelText>
                    </FormControlLabel>
                    <Input>
                      <InputField
                        type="number"
                        keyboardType="numeric"
                        onChangeText={(text) =>
                          handleInputTextChange("port", text)
                        }
                      >
                        {formInputsRef.current.port}
                      </InputField>
                    </Input>
                    <FormControlError>
                      <FormControlErrorIcon as={AlertCircleIcon} />
                      <FormControlErrorText>
                        {formState.port.errorText}
                      </FormControlErrorText>
                    </FormControlError>
                  </FormControl>

                  {/* Nickname */}
                  <FormControl
                    size="md"
                    isDisabled={formState.form.isDisabled}
                    isInvalid={formState.nickname.isInvalid}
                    isRequired={true}
                  >
                    <FormControlLabel mb="$1">
                      <FormControlLabelText>Nickname</FormControlLabelText>
                    </FormControlLabel>
                    <Input>
                      <InputField
                        type="text"
                        onChangeText={(text) =>
                          handleInputTextChange("nickname", text)
                        }
                      >
                        {formInputsRef.current.nickname}
                      </InputField>
                    </Input>
                    <FormControlError>
                      <FormControlErrorIcon as={AlertCircleIcon} />
                      <FormControlErrorText>
                        <FormControlErrorText>
                          {formState.nickname.errorText}
                        </FormControlErrorText>
                      </FormControlErrorText>
                    </FormControlError>
                  </FormControl>
                </VStack>

                {/* Button */}
                <Button
                  size="md"
                  variant="solid"
                  action="primary"
                  isDisabled={formState.form.isDisabled}
                  onPress={handleFormSubmit}
                >
                  {!formState.form.isDisabled ? (
                    <ButtonIcon as={GlobeIcon} />
                  ) : (
                    <ButtonSpinner />
                  )}
                  <ButtonText> {formState.button.text}</ButtonText>
                </Button>
              </VStack>
            </Box>
          </VStack>
        </Center>
      </KeyboardAvoidingView>

      <Modal isOpen={showErrorModal} size="md">
        <ModalBackdrop />
        <ModalContent>
          <ModalHeader>
            <Heading size="xl">Connection Error</Heading>
          </ModalHeader>
          <ModalBody>
            <Text>
              Cannot establish a connection to the server. Please check the
              internet connection, validity of the input and try again later.
            </Text>
          </ModalBody>
          <ModalFooter>
            <Button
              variant="solid"
              size="sm"
              action="secondary"
              onPress={() => {
                setShowErrorModal(false);
              }}
            >
              <ButtonText>OK</ButtonText>
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </>
  );
};

export default FormConnect;
