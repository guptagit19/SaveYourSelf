// File: src/components/AccessSetupModal.js
import React, { useState, useContext, useEffect } from 'react';
import {
  View,
  Text,
  Modal,
  StyleSheet,
  TouchableOpacity,
  Platform,
} from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { AppContext } from '../context/AppContext';

export default function AccessSetupModal({ packageName, onClose }) {
  const { selectedApps, accessRules, setRule } = useContext(AppContext);
  const [accessDuration, setAccessDuration] = useState(5 * 60 * 1000); // default 5min
  const [lockDuration, setLockDuration] = useState(30 * 60 * 1000); // default 30min

  const app = selectedApps.find(a => a.packageName === packageName);

  const confirm = () => {
    const now = Date.now();
    const accessEnd = now + accessDuration;
    const lockEnd = accessEnd + lockDuration;
    console.log('[DEBUG][AccessSetup] setting rule for', packageName, {
      accessEnd,
      lockEnd,
      lockDuration,
    });
    setRule(packageName, { accessEnd, lockEnd, lockDuration });
    onClose();
  };

  if (!app) return null;

  return (
    <Modal transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.container}>
          <Text style={styles.title}>Set Access & Lock Time</Text>
          <Text style={styles.label}>Access Time (minutes):</Text>
          {/* Replace with sliders or pickers as desired */}
          <View style={styles.row}>
            <TouchableOpacity
              onPress={() => setAccessDuration(prev => prev + 60 * 1000)}
            >
              <Text style={styles.adjust}>＋</Text>
            </TouchableOpacity>
            <Text style={styles.value}>
              {Math.floor(accessDuration / 60000)}
            </Text>
            <TouchableOpacity
              onPress={() =>
                setAccessDuration(prev => Math.max(60 * 1000, prev - 60 * 1000))
              }
            >
              <Text style={styles.adjust}>－</Text>
            </TouchableOpacity>
          </View>

          <Text style={[styles.label, { marginTop: 16 }]}>
            Lock Duration (minutes):
          </Text>
          <View style={styles.row}>
            <TouchableOpacity
              onPress={() => setLockDuration(prev => prev + 60 * 1000)}
            >
              <Text style={styles.adjust}>＋</Text>
            </TouchableOpacity>
            <Text style={styles.value}>{Math.floor(lockDuration / 60000)}</Text>
            <TouchableOpacity
              onPress={() =>
                setLockDuration(prev => Math.max(60 * 1000, prev - 60 * 1000))
              }
            >
              <Text style={styles.adjust}>－</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.buttons}>
            <TouchableOpacity style={styles.btnCancel} onPress={onClose}>
              <Text style={styles.btnText}>Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.btnConfirm} onPress={confirm}>
              <Text style={styles.btnText}>Confirm</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.6)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  container: {
    width: '80%',
    backgroundColor: '#222',
    borderRadius: 12,
    padding: 20,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#fff',
    marginBottom: 12,
    textAlign: 'center',
  },
  label: {
    color: '#ccc',
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginVertical: 4,
  },
  adjust: {
    fontSize: 24,
    color: '#fff',
    paddingHorizontal: 12,
  },
  value: {
    fontSize: 20,
    color: '#fff',
  },
  buttons: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 20,
  },
  btnCancel: {
    marginRight: 16,
  },
  btnConfirm: {
    backgroundColor: '#3498db',
    borderRadius: 6,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  btnText: {
    color: '#fff',
    fontWeight: '600',
  },
});
