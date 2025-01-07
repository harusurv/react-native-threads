import React from 'react';
import {SafeAreaView, Text} from 'react-native';
import {Thread} from 'react-native-threads';

function App(): JSX.Element {
  const [reactNativeExports, setReactNativeExports] = React.useState<
    readonly string[]
  >([]);
  React.useEffect(
    () =>
      void new Promise(onmessage =>
        Object.assign(new Thread('index.thread.js'), {onmessage}).postMessage(
          JSON.stringify({}),
        ),
      ).then(e => setReactNativeExports(JSON.parse(String(e)))),
    [],
  );
  return (
    <SafeAreaView>
      <Text children={JSON.stringify(reactNativeExports)} />
    </SafeAreaView>
  );
}

export default App;
