// File: src/components/AppListItem.js
import React, { useContext } from 'react';
import { View, Text, Image, StyleSheet, TouchableOpacity } from 'react-native';
import CheckBox from '@react-native-community/checkbox';
import { AppContext } from '../context/AppContext';

export default function AppListItem({ app }) {
  const { selectedApps, toggleApp } = useContext(AppContext);
  const isSelected = selectedApps.some(a => a.packageName === app.packageName);

  console.log(
    '[DEBUG][AppListItem]',
    app.packageName,
    'selected?',
    isSelected,
  );

  return (
    <TouchableOpacity
      style={styles.row}
      activeOpacity={0.7}
      onPress={() => toggleApp(app)}
    >
      <Image
        source={{ uri: `data:image/png;base64,${app.icon}` }}
        style={styles.icon}
      />
      <Text style={styles.name}>{app.name}</Text>
      <CheckBox value={isSelected} onValueChange={() => toggleApp(app)} />
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderBottomWidth: 0.5,
    borderColor: '#333',
  },
  icon: {
    width: 32,
    height: 32,
    marginRight: 12,
    borderRadius: 6,
  },
  name: {
    flex: 1,
    fontSize: 16,
    color: '#fff',
  },
});
