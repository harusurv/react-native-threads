import * as RN from 'react-native';
import {self} from 'react-native-threads';

self.onmessage = message => self.postMessage(JSON.stringify(Object.keys(RN)));
