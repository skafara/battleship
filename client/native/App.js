import {
  StatusBar,
  GluestackUIProvider,
  SafeAreaView,
} from "@gluestack-ui/themed";
import { config } from "@gluestack-ui/config";
import Main from "./components/Main";

/**
 * Application entry point
 */
export default () => {
  return (
    <GluestackUIProvider config={config}>
      <StatusBar />
      <SafeAreaView flex>
        <Main />
      </SafeAreaView>
    </GluestackUIProvider>
  );
};
