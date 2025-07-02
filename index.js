/**
 * @format
 */
//index.js
import 'react-native-gesture-handler';
import { AppRegistry } from 'react-native';
import App from './App';
import { name as appName } from './app.json';
import './SetupOverlayRoot'; // This registers the 'SetupOverlay' component

AppRegistry.registerComponent(appName, () => App);
