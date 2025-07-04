// File: src/components/AccessSetupModel.js
import React, { useState, useContext } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  Platform,
  NativeModules,
} from 'react-native';
import { AppContext } from '../context/AppContext';

const { AppUtilsModule } = NativeModules;

export default function SetupOverlay({ packageName }) {
  const { selectedApps, setRule } = useContext(AppContext);
  const [accessDuration, setAccessDuration] = useState(5 * 60_000);
  const [lockDuration, setLockDuration] = useState(30 * 60_000);

  // find the full app object so we have icon & name
  const app = selectedApps.find(a => a.packageName === packageName);
  if (!app) return null;

  const confirm = () => {
    const now = Date.now();
    const accessEnd = now + accessDuration;
    const lockEnd = accessEnd + lockDuration;

    // 1) Persist your access rule
    setRule(packageName, { accessEnd, lockEnd, lockDuration });
    // 2) Finish the Android Activity (now LockScreenActivity)
    AppUtilsModule.finishLockScreenActivity();
  };

  return (
    <View style={styles.container}>
      {/* header */}
      <View style={styles.header}>
        <Image
          source={{ uri: `data:image/png;base64,${app.icon}` }}
          style={styles.icon}
        />
        <Text style={styles.appName}>{app.name}</Text>
      </View>

      {/* pickers */}
      <View style={styles.section}>
        <Text style={styles.label}>Access Time (min):</Text>
        <View style={styles.row}>
          <TouchableOpacity onPress={() => setAccessDuration(d => d + 60_000)}>
            <Text style={styles.adjust}>＋</Text>
          </TouchableOpacity>
          <Text style={styles.value}>
            {Math.floor(accessDuration / 60_000)}
          </Text>
          <TouchableOpacity
            onPress={() => setAccessDuration(d => Math.max(60_000, d - 60_000))}
          >
            <Text style={styles.adjust}>－</Text>
          </TouchableOpacity>
        </View>

        <Text style={[styles.label, { marginTop: 24 }]}>Lock Time (min):</Text>
        <View style={styles.row}>
          <TouchableOpacity onPress={() => setLockDuration(d => d + 60_000)}>
            <Text style={styles.adjust}>＋</Text>
          </TouchableOpacity>
          <Text style={styles.value}>{Math.floor(lockDuration / 60_000)}</Text>
          <TouchableOpacity
            onPress={() => setLockDuration(d => Math.max(60_000, d - 60_000))}
          >
            <Text style={styles.adjust}>－</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* actions */}
      <View style={styles.actions}>
        <TouchableOpacity
          style={styles.btnCancel}
          onPress={() => AppUtilsModule.finishLockScreenActivity()}
        >
          <Text style={styles.btnText}>Cancel</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btnConfirm} onPress={confirm}>
          <Text style={styles.btnText}>Confirm</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#111',
    paddingTop: Platform.select({ ios: 50, android: 20 }),
    alignItems: 'center',
  },
  header: {
    alignItems: 'center',
    marginBottom: 32,
  },
  icon: {
    width: 80,
    height: 80,
    borderRadius: 20,
    marginBottom: 12,
  },
  appName: {
    fontSize: 24,
    color: '#fff',
    fontWeight: '600',
  },
  section: {
    width: '80%',
  },
  label: {
    color: '#ccc',
    fontSize: 16,
    marginBottom: 8,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  adjust: {
    fontSize: 28,
    color: '#fff',
    paddingHorizontal: 16,
  },
  value: {
    fontSize: 22,
    color: '#fff',
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '80%',
    marginTop: 40,
  },
  btnCancel: {
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 6,
    backgroundColor: '#444',
  },
  btnConfirm: {
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 6,
    backgroundColor: '#28a745',
  },
  btnText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
