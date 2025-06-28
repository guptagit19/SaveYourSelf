// File: src/components/SectionHeader.js
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function SectionHeader({ title }) {
  console.log('[DEBUG][SectionHeader] section:', title);
  console.debug('[DEBUG][SectionHeader] section:', title);
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingVertical: 8,
    paddingHorizontal: 12,
    backgroundColor: '#1a1a1a',
  },
  title: {
    fontSize: 14,
    fontWeight: '600',
    color: '#bbb',
  },
});
