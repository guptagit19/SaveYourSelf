//src/services/AppContext.js
import React, { createContext, useState, useEffect } from 'react';
import Storage, { DISTRACTING_APPS_KEY } from '../services/storage';
import { NativeModules } from 'react-native';

export const AppContext = createContext({
  selectedApps: [],
  accessRules: {},
  isLoading: true,
  toggleApp: () => {},
  setRule: () => {},
});

const { AppUtilsModule } = NativeModules;

export const AppProvider = ({ children }) => {
  const [selectedApps, setSelectedApps] = useState([]);
  const [accessRules, setAccessRules] = useState({});
  const [isLoading, setIsLoading] = useState(true);

  // 1) Load persisted data on mount
  useEffect(() => {
    (async () => {
      console.log('[DEBUG][AppContext] Loading storage...');
      const apps = (await Storage.getArrayAsync(DISTRACTING_APPS_KEY)) || [];
      const rulesJson = (await Storage.getStringAsync('accessRules')) || '{}';
      const rules = JSON.parse(rulesJson);
      setSelectedApps(apps);
      setAccessRules(rules);
      setIsLoading(false);

      console.log(
        '[DEBUG][AppContext] Loaded',
        apps.length,
        'apps, rules:',
        rules,
      );
    })();
  }, []);

  useEffect(() => {
    if (!isLoading) {
      // ② whenever rules change, push them to native
      console.log('[DEBUG][AppContext] pushing rules→native', accessRules);
      AppUtilsModule.updateAccessRules(JSON.stringify(accessRules));
    }
  }, [isLoading, accessRules]);

  // 2) Toggle selected apps and persist
  const toggleApp = async app => {
    console.log('[DEBUG][AppContext] toggleApp:', app.packageName);
    let nextSelected;
    let nextRules = { ...accessRules };

    if (selectedApps.some(a => a.packageName === app.packageName)) {
      // Unselecting: remove from selectedApps
      nextSelected = selectedApps.filter(
        a => a.packageName !== app.packageName,
      );
      // Also delete its rule
      delete nextRules[app.packageName];
      setAccessRules(nextRules);
      // Persist updated rules
      await Storage.setStringAsync('accessRules', JSON.stringify(nextRules));
    } else {
      // Selecting: add to selectedApps
      nextSelected = [...selectedApps, app];
      // Give it an empty placeholder rule
      nextRules[app.packageName] = {};
      setAccessRules(nextRules);
      await Storage.setStringAsync('accessRules', JSON.stringify(nextRules));
    }

    setSelectedApps(nextSelected);
    await Storage.setArrayAsync(DISTRACTING_APPS_KEY, nextSelected);

    console.log('[DEBUG][AppContext] Persisted distractingApps:', nextSelected);
    console.log('[DEBUG][AppContext] Persisted accessRules:', nextRules);
  };

  // 3) Set or update access/lock rules and persist
  const setRule = async (packageName, rule) => {
    const updated = { ...accessRules, [packageName]: rule };
    setAccessRules(updated);
    await Storage.setStringAsync('accessRules', JSON.stringify(updated));
    console.log('[DEBUG][AppContext] Persisted accessRules:', updated);
  };

  return (
    <AppContext.Provider
      value={{ selectedApps, accessRules, isLoading, toggleApp, setRule }}
    >
      {children}
    </AppContext.Provider>
  );
};
