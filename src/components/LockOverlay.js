// File: src/components/LockOverlay.js
import React, { useEffect, useState, useContext } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  TouchableOpacity,
  Dimensions,
} from 'react-native';
import { AppContext } from '../context/AppContext';

const { width, height } = Dimensions.get('window');

export default function LockOverlay({ packageName, onClose }) {
  const { selectedApps, accessRules } = useContext(AppContext);
  const [app, setApp] = useState(null);
  const [timeLeft, setTimeLeft] = useState(0);

  useEffect(() => {
    const found = selectedApps.find(a => a.packageName === packageName);
    if (found) {
      setApp(found);
      const rule = accessRules[packageName] || {};
      const now = Date.now();
      const remaining = Math.max(0, (rule.lockEnd || now) - now);
      setTimeLeft(remaining);
      console.debug(
        '[DEBUG][LockOverlay] lockEnd:',
        rule.lockEnd,
        'timeLeft ms:',
        remaining,
      );
      // countdown
      const interval = setInterval(() => {
        setTimeLeft(prev => Math.max(0, prev - 1000));
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [packageName, selectedApps, accessRules]);

  if (!app) return null;

  const minutes = Math.floor(timeLeft / 60000);
  const seconds = Math.floor((timeLeft % 60000) / 1000);
  const timer = `${minutes.toString().padStart(2, '0')}:${seconds
    .toString()
    .padStart(2, '0')}`;

  return (
    <View style={styles.overlay}>
      <View style={styles.container}>
        <Image
          source={{ uri: `data:image/png;base64,${app.icon}` }}
          style={styles.icon}
        />
        <Text style={styles.title}>{app.name} is Locked</Text>
        <Text style={styles.timer}>{timer}</Text>
        <Text style={styles.message}>
          Stay focused! You will regain access when the timer expires.
        </Text>
        <TouchableOpacity style={styles.button} onPress={onClose}>
          <Text style={styles.buttonText}>OK</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    width,
    height,
    backgroundColor: 'rgba(0,0,0,0.8)',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  },
  container: {
    width: '80%',
    backgroundColor: '#222',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
  },
  icon: {
    width: 64,
    height: 64,
    marginBottom: 16,
    borderRadius: 12,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#fff',
    marginBottom: 8,
  },
  timer: {
    fontSize: 36,
    fontWeight: '700',
    color: '#e74c3c',
    marginBottom: 12,
  },
  message: {
    fontSize: 16,
    color: '#ccc',
    textAlign: 'center',
    marginBottom: 20,
    lineHeight: 22,
  },
  button: {
    backgroundColor: '#e74c3c',
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 24,
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
  },
});
