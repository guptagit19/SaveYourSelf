//src/services/uiManager.js
import { DeviceEventEmitter } from 'react-native';

// JS wrappers to signal App.js to show overlays
export function showLockScreen(packageName) {
  console.log('[DEBUG][uiManager] showLockScreen', packageName);
  DeviceEventEmitter.emit('SHOW_LOCK_SCREEN', { packageName });
}

export function showAccessSetup(packageName) {
  console.log('[DEBUG][uiManager] showAccessSetup', packageName);
  DeviceEventEmitter.emit('SHOW_ACCESS_SETUP', { packageName });
}
