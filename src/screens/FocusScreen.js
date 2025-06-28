import React, { useContext } from 'react';
import {
  View,
  Text,
  SectionList,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';
import SectionHeader from '../components/SectionHeader';
import AppListItem from '../components/AppListItem';
import useInstalledApps from '../hooks/useInstalledApps';
import { AppContext } from '../context/AppContext';

export default function FocusScreen() {
  const { apps, error } = useInstalledApps();
  const { selectedApps, toggleApp, isLoading } = useContext(AppContext);

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
      </View>
    );
  }
  if (error) {
    return (
      <View style={styles.center}>
        <Text>Error loading apps</Text>
      </View>
    );
  }

  const remaining = apps.filter(
    a => !selectedApps.some(s => s.packageName === a.packageName),
  );
  const sections = [
    { title: 'Your distracting apps', data: selectedApps },
    { title: 'Select more apps', data: remaining },
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Focus</Text>
      <Text style={styles.subtitle}>Pause selected apps to stay on task</Text>
      <SectionList
        sections={sections}
        keyExtractor={item => item.packageName}
        renderSectionHeader={({ section }) => (
          <SectionHeader title={section.title} />
        )}
        renderItem={({ item }) => <AppListItem app={item} />}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000' },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
    marginTop: 20,
  },
  subtitle: {
    fontSize: 14,
    color: '#ccc',
    textAlign: 'center',
    marginBottom: 12,
  },
});
