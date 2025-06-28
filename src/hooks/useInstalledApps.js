import { useEffect, useState } from 'react';
import { NativeModules } from 'react-native';
console.log('Registered NativeModules:', Object.keys(NativeModules));
const { AppUtilsModule } = NativeModules;

export default function useInstalledApps() {
  const [apps, setApps] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    async function fetchApps() {
      try {
        console.debug('[DEBUG][useInstalledApps] Fetching installed apps');
        const jsonString = await AppUtilsModule.getInstalledApps();
        console.debug('[DEBUG][useInstalledApps] Raw JSON:', jsonString);
        const parsed = JSON.parse(jsonString);
        if (!Array.isArray(parsed)) throw new Error('Expected array');
        if (mounted) {
          console.debug(
            '[DEBUG][useInstalledApps] Parsed apps count:',
            parsed.length,
          );
          setApps(parsed);
        }
      } catch (e) {
        console.error('[ERROR][useInstalledApps]', e);
        if (mounted) setError(e);
      }
    }
    fetchApps();
    return () => {
      mounted = false;
    };
  }, []);

  return { apps, error };
}
