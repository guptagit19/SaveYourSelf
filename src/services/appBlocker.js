//src/services/appBlocker.js
import Storage, { DISTRACTING_APPS_KEY, ACCESS_RULES_KEY } from './storage';
import { showLockScreen, showAccessSetup } from './uiManager';

export async function checkAppBlocking(packageName) {
  console.log('[DEBUG][appBlocker] check', packageName);
  const blocked = (await Storage.getArrayAsync(DISTRACTING_APPS_KEY)) || [];
  if (!blocked.find(a => a.packageName === packageName)) return;

  const rulesJson = (await Storage.getStringAsync(ACCESS_RULES_KEY)) || '{}';
  const rules = JSON.parse(rulesJson)[packageName] || {};
  const now = Date.now();

  if (!rules.accessEnd) {
    console.log('[DEBUG][appBlocker] no rules');
    return showAccessSetup(packageName);
  }

  if (now < rules.accessEnd) {
    console.log('[DEBUG][appBlocker] within access');
    return; // you could start a JS timer here
  }

  if (!rules.lockEnd || now < rules.lockEnd) {
    console.log('[DEBUG][appBlocker] locking');
    return showLockScreen(packageName);
  }

  console.log('[DEBUG][appBlocker] rule expired entirely');
}
