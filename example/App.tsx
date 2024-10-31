import React from 'react';
import {StyleSheet, View} from 'react-native';
import {Thread} from '@exodus/react-native-threads';

function App(): JSX.Element {
  const [backgroundColor, setBackgroundColor] = React.useState<string>();
  React.useEffect(
    () =>
      void new Promise(onmessage => {
        Object.assign(new Thread('index.thread.js'), {
          onmessage,
        }).postMessage(JSON.stringify(true));
      })
        .then(() => setBackgroundColor('green'))
        .catch(() => setBackgroundColor('red')),
    [],
  );
  return <View style={[StyleSheet.absoluteFill, {backgroundColor}]} />;
}

export default App;
