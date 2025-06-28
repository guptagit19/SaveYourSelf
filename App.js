//App.js
import React, { useState, useEffect, useContext } from 'react';
import { DeviceEventEmitter } from 'react-native';
import { AppProvider, AppContext } from './src/context/AppContext';
import FocusScreen from './src/screens/FocusScreen';
import LockOverlay from './src/components/LockOverlay';
import AccessSetupModal from './src/components/AccessSetupModal';
import { startAppMonitoring } from './src/services/appMonitor';
import { debugStorage } from './src/services/storage';

export default function App() {
  // Provider is top-level
  return (
    <AppProvider>
      <InnerApp />
    </AppProvider>
  );
}

function InnerApp() {
  const [lockedApp, setLockedApp] = useState(null);
  const [setupApp, setSetupApp] = useState(null);

  // Now this useContext is INSIDE the provider
  const { accessRules, isLoading } = useContext(AppContext);

  useEffect(() => {
    if (!isLoading) {
      console.log('[DEBUG][App] Storage loaded – starting monitor');
      console.debug('[DEBUG][App] Storage loaded – starting monitor');
      debugStorage();
      startAppMonitoring();
    }
  }, [isLoading]);

  useEffect(() => {
    console.log('[DEBUG][App] useEffect ', 'DeviceEventEmitter');
    const subLock = DeviceEventEmitter.addListener(
      'SHOW_LOCK_SCREEN',
      ({ packageName }) => setLockedApp(packageName),
    );
    const subSetup = DeviceEventEmitter.addListener(
      'SHOW_ACCESS_SETUP',
      ({ packageName }) => setSetupApp(packageName),
    );

    const sub = DeviceEventEmitter.addListener('NATIVE_LOG', msg => {
      console.log('[NATIVE_LOG]', msg);
    });

    return () => {
      subLock.remove();
      subSetup.remove();
      sub.remove();
    };
  }, []);

  return (
    <>
      <FocusScreen />
      {lockedApp && (
        <LockOverlay
          packageName={lockedApp}
          onClose={() => setLockedApp(null)}
        />
      )}
      {setupApp && (
        <AccessSetupModal
          packageName={setupApp}
          onClose={() => setSetupApp(null)}
        />
      )}
    </>
  );
}
