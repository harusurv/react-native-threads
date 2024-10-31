import {self} from '@exodus/react-native-threads';

self.onmessage = message => self.postMessage(message);
