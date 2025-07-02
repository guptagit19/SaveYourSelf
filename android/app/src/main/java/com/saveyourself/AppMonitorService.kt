package com.saveyourself

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson

class AppMonitorService : Service() {
  companion object {
    private const val CHANNEL_ID_MONITOR = "app_monitor_channel"
    private const val NOTIF_ID_MONITOR   = 1
    private const val MAX_OVERLAY_RETRIES = 10 // Increased max retries
    private const val OVERLAY_RETRY_DELAY_MS = 200L // Reduced delay for faster retries
  }

  private lateinit var usageStats: UsageStatsManager
  private var running = true
  private var lastPkg: String? = null
  private val handler = Handler(Looper.getMainLooper()) // Handler for posting delayed tasks

  override fun onCreate() {
    super.onCreate()
    usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    createMonitorChannel()
    startForeground(NOTIF_ID_MONITOR, buildMonitorNotification())
    monitorLoop()
  }

  private fun createMonitorChannel() {
    val nm = getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      nm.createNotificationChannel(
        NotificationChannel(CHANNEL_ID_MONITOR, "App Monitor", NotificationManager.IMPORTANCE_LOW)
      )
    }
  }

  private fun buildMonitorNotification(): Notification =
    NotificationCompat.Builder(this, CHANNEL_ID_MONITOR)
      .setContentTitle("Focus Mode Active")
      .setContentText("Watching your app usage…")
      .setSmallIcon(R.mipmap.ic_launcher)
      .build()

  private fun monitorLoop() {
    Log.d("AppMonitorService", "monitorLoop")
    Thread {
      while (running) {
        try {
          val now = System.currentTimeMillis()
          val events = usageStats.queryEvents(now - 2000, now)
          val ev = UsageEvents.Event()
          var fgPkg: String? = null
          while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
              fgPkg = ev.packageName
            }
          }
          fgPkg?.let { pkg ->
            if (pkg != lastPkg) { // Only log and check if package changed
              Log.d("AppMonitorService", "Foreground = $pkg")
              if (shouldBlock(pkg)) {
                Log.d("AppMonitorService", "shouldBlock inside = $pkg")
                // Directly call the OverlayModule to show the lock screen
                showLockScreenOverlay(pkg, 0) // Start with retry count 0
              }
              lastPkg = pkg
            }
          }

          Thread.sleep(1000)
        } catch (t: Throwable) {
          Log.e("AppMonitorService", "Loop error", t)
        }
      }
    }.start()
  }

  private fun shouldBlock(pkg: String): Boolean {
    // Prevent blocking your own app or the Android system launcher/UI
    if (pkg == packageName ||
      pkg == "com.google.android.launcher" ||
      pkg == "com.android.launcher3" ||
      pkg == "com.android.systemui" ||
      pkg == "com.google.android.gms" || // Google Play Services
      pkg == "android" // Android system process
    ) {
      return false
    }

    val rulesJson = AppUtilsModule.currentRulesJson
    val map: Map<String, Any> =
      Gson().fromJson(rulesJson, Map::class.java) as Map<String, Any>
    val pkgRule = map[pkg] as? Map<*, *> ?: return false
    // 3) If it’s in the map *but* empty → trigger the initial setup
    if (pkgRule.isEmpty()) {
      Log.d("AppMonitorService", "First time seeing $pkg, showing setup")
      return true
    }
    // now check pkgRule["accessEnd"] and pkgRule["lockEnd"]
    val now = System.currentTimeMillis()
    val accessEnd = (pkgRule["accessEnd"] as? Double)?.toLong() ?: 0L
    val lockEnd    = (pkgRule["lockEnd"]    as? Double)?.toLong() ?: 0L
    return now > accessEnd && now < lockEnd
  }

  // Modified function to retry showing the overlay
  private fun showLockScreenOverlay(pkg: String, retryCount: Int) {
    if (retryCount >= MAX_OVERLAY_RETRIES) {
      Log.e("AppMonitorService", "Max retries reached for showing overlay for $pkg. Aborting.")
      return
    }

    Log.d("AppMonitorService", "Attempting to show overlay for $pkg (Retry: $retryCount)")
    // Get the ReactContext from MainApplication
    val reactContext = (application as MainApplication).getActiveReactContext()

    if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
      val overlayModule = reactContext.getNativeModule(OverlayModule::class.java)
      overlayModule?.showLockScreen(pkg) // Call directly
    } else {
      Log.w("AppMonitorService", "ReactContext not available or not active. Retrying in ${OVERLAY_RETRY_DELAY_MS}ms (Retry: ${retryCount + 1})...")
      handler.postDelayed({
        showLockScreenOverlay(pkg, retryCount + 1)
      }, OVERLAY_RETRY_DELAY_MS)
    }
  }

  override fun onDestroy() {
    running = false
    handler.removeCallbacksAndMessages(null) // Clean up any pending handler messages
    // Ensure overlay is hidden when service is destroyed
    val reactContext = (application as MainApplication).getActiveReactContext()
    if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
      val overlayModule = reactContext.getNativeModule(OverlayModule::class.java)
      overlayModule?.hideLockScreen() // Call directly
    }
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
