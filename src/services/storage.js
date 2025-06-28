//src/services/storage.js
import { MMKVLoader } from 'react-native-mmkv-storage';

// Initialize MMKV
const Storage = new MMKVLoader().withEncryption().initialize();
export const DISTRACTING_APPS_KEY = 'distractingApps';
export const ACCESS_RULES_KEY = 'accessRules';

// Debug helper
export const debugStorage = async () => {
  console.log('[DEBUG][Storage] Dumping keys...');
  const keys = await Storage.indexer.getKeys();
  for (const k of keys) {
    const val = await Storage.getStringAsync(k).catch(() =>
      Storage.getArrayAsync(k),
    );
    console.log('[DEBUG][Storage]', k, ':', val);
  }
};

export default Storage;
