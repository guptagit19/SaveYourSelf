import React, { useEffect, useContext } from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  // Removed DeviceEventEmitter as overlay is handled natively
  // DeviceEventEmitter,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import { AppProvider, AppContext } from './src/context/AppContext';
import FocusScreen from './src/screens/FocusScreen';
// Removed these as they are now handled by the native overlay
// import LockOverlay from './src/components/LockOverlay';
// import AccessSetupModal from './src/components/AccessSetupModal';
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
  // Removed states for lockedApp and setupApp as they are handled by the native overlay
  // const [lockedApp, setLockedApp] = useState(null);
  // const [setupApp, setSetupApp] = useState(null);

  // Now this useContext is INSIDE the provider
  const { accessRules, isLoading } = useContext(AppContext);

  useEffect(() => {
    if (!isLoading) {
      console.log('[DEBUG][App] Storage loaded – starting monitor');
      console.log('[DEBUG][App] accessRules – ', accessRules);
      debugStorage();
      // startAppMonitoring will now pass rules to the native service
      // and the service will directly show the overlay if needed.
      startAppMonitoring(accessRules);
    }
  }, [isLoading, accessRules]);

  useEffect(() => {
    // Removed all DeviceEventEmitter listeners as the native overlay handles the lock screen directly.
    console.log('[DEBUG][App] DeviceEventEmitter listeners removed for lock screen logic.');

    // If you have other NATIVE_LOG events you still want to listen to:
    // const sub = DeviceEventEmitter.addListener('NATIVE_LOG', msg => {
    //   console.log('[NATIVE_LOG]', msg);
    // });
    // return () => {
    //   sub.remove();
    // };
    return () => {}; // Empty cleanup if no listeners remain
  }, []);

  return (
    <>
      <FocusScreen />
      {/* Removed conditional rendering of LockOverlay and AccessSetupModal */}
      {/* These components are now rendered by the native OverlayModule */}
    </>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});
