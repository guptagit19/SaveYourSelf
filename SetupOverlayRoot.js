import React from 'react';
import { AppRegistry, NativeModules } from 'react-native';
import { AppProvider } from './src/context/AppContext';
import AccessSetupModal from './src/components/AccessSetupModal';

export default function SetupOverlayRoot(props) {
  // props.packageName comes from the initialProps passed by OverlayModule
  console.log('SetupOverlayRoot launched with packageName:', props.packageName);

  return (
    <AppProvider>
      <AccessSetupModal
        packageName={props.packageName}
        onClose={() => {
          console.log('AccessSetupModal onClose called. Hiding native overlay.');
          // Call the native OverlayModule to hide the overlay
          NativeModules.OverlayModule.hideLockScreen();
        }}
      />
    </AppProvider>
  );
}

// register this as the entry point for the native OverlayModule's ReactRootView
AppRegistry.registerComponent('SetupOverlay', () => SetupOverlayRoot);
