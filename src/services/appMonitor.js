//src/services/appMonitor.js
import {
  NativeModules,
  NativeEventEmitter,
  DeviceEventEmitter,
} from 'react-native';
import { checkAppBlocking } from './appBlocker';

const { AppUtilsModule } = NativeModules;
const appEmitter = new NativeEventEmitter(AppUtilsModule);

let fgSubscription = null;

// 1) Listen for our debug-bridge messages:
DeviceEventEmitter.addListener('NATIVE_LOG', msg => {
  console.log('[NATIVE_LOG]', msg);
});

// 2) Start/stop the actual app-foreground monitor:
export function startAppMonitoring() {
  console.log('[DEBUG][appMonitor] start');
  AppUtilsModule.startAppMonitoring();

  // when you start monitoring:
  DeviceEventEmitter.removeAllListeners('APP_IN_FOREGROUND');
  fgSubscription = DeviceEventEmitter.addListener(
    'APP_IN_FOREGROUND',
    packageName => {
      console.log('[DEBUG][appMonitor] FG:', packageName);
      checkAppBlocking(packageName);
    },
  );
}

export function stopAppMonitoring() {
  console.log('[DEBUG][appMonitor] stop');
  AppUtilsModule.stopAppMonitoring();

  fgSubscription?.remove();
  DeviceEventEmitter.removeAllListeners('APP_IN_FOREGROUND');
}
