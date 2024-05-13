import {
  StatusBar,
  GluestackUIProvider,
  SafeAreaView,
} from "@gluestack-ui/themed";
import { config } from "@gluestack-ui/config";
import Main from "./components/Main";

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
