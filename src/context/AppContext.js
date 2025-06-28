//src/services/AppContext.js
import React, { createContext, useState, useEffect } from 'react';
import Storage, { DISTRACTING_APPS_KEY } from '../services/storage';

export const AppContext = createContext({
  selectedApps: [],
  accessRules: {},
  isLoading: true,
  toggleApp: () => {},
  setRule: () => {},
});

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
      console.debug(
        '[DEBUG][AppContext] Loaded',
        apps.length,
        'apps, rules:',
        rules,
      );

    })();
  }, []);

  // 2) Toggle selected apps and persist
  const toggleApp = async app => {
    console.log('[DEBUG][AppContext] toggleApp:', app.packageName);
    let next;
    if (selectedApps.some(a => a.packageName === app.packageName)) {
      next = selectedApps.filter(a => a.packageName !== app.packageName);
    } else {
      next = [...selectedApps, app];
    }
    setSelectedApps(next);
    await Storage.setArrayAsync(DISTRACTING_APPS_KEY, next);
    console.log('[DEBUG][AppContext] Persisted distractingApps:', next);
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
