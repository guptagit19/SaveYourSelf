// SetupOverlayRoot.js
import React from 'react';
import { AppRegistry, NativeModules } from 'react-native';
import { AppProvider } from './src/context/AppContext';
import AccessSetupModal from './src/components/AccessSetupModal';

export default function SetupOverlayRoot(props) {
  // props.packageName comes from getLaunchOptions()
  return (
    <AppProvider>
      <AccessSetupModal
        packageName={props.packageName}
        onClose={() => {
          // You can close the Activity by sending a NativeEvent
          // or by calling a native method that finishes the Activity.
          // E.g. NativeModules.AppUtilsModule.finishSetupActivity()
          // 2) Then finish the Android activity:
          NativeModules.AppUtilsModule.finishSetupActivity();
        }}
      />
    </AppProvider>
  );
}

// register this as the entry point:
AppRegistry.registerComponent('SetupOverlay', () => SetupOverlayRoot);
