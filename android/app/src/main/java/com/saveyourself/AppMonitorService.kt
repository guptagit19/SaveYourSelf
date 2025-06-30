package com.saveyourself

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.facebook.react.ReactApplication
import com.google.gson.Gson

class AppMonitorService : Service() {
  companion object {
    private const val CHANNEL_ID_MONITOR   = "app_monitor_channel"
    private const val CHANNEL_ID_BLOCK     = "app_block_channel"
    private const val NOTIF_ID_MONITOR     = 1
    private const val NOTIF_ID_BLOCK_BASE  = 1000
  }

  private lateinit var usageStats: UsageStatsManager
  private var running = true
  private var blockCount = 0
  private var lastPkg: String? = null


  override fun onCreate() {
    super.onCreate()
    usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    createChannels()
    startForeground(NOTIF_ID_MONITOR, buildMonitorNotification())
    monitorLoop()
  }

  private fun createChannels() {
    val nm = getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      nm.createNotificationChannel(
        NotificationChannel(CHANNEL_ID_MONITOR, "App Monitor", NotificationManager.IMPORTANCE_LOW)
      )
      nm.createNotificationChannel(
        NotificationChannel(CHANNEL_ID_BLOCK, "Blocked App", NotificationManager.IMPORTANCE_HIGH)
          .apply {
            // full‑screen allowed
            setBypassDnd(true)
            setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
          }
      )
    }
  }

  private fun buildMonitorNotification(): Notification =
    NotificationCompat.Builder(this, CHANNEL_ID_MONITOR)
      .setContentTitle("Focus Mode Active")
      .setContentText("Watching your app usage…")
      .setSmallIcon(R.mipmap.ic_launcher)
      .build()

  private fun buildBlockNotification(pkg: String): Notification {
    Log.d("AppMonitorService", "buildBlockNotification pkg - ${pkg}")
    // Intent to launch your transparent SetupActivity
    val fullScreenIntent = Intent(this, SetupActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("pkg", pkg)
    }
    val fullScreenPending =
      PendingIntent.getActivity(this, blockCount++, fullScreenIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    return NotificationCompat.Builder(this, CHANNEL_ID_BLOCK)
      .setContentTitle("App Blocked")
      .setContentText("Access to $pkg is blocked")
      .setSmallIcon(R.mipmap.ic_launcher)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_CALL)        // “full‑screen” category
      .setFullScreenIntent(fullScreenPending, true)
      .build()
  }

  private fun monitorLoop() {
    Log.d("AppMonitorService", "monitorLoop")
    Thread {
      while (running) {
        try {
          val now = System.currentTimeMillis()
          val events = usageStats.queryEvents(now - 2000, now)
          val ev = UsageEvents.Event()
          var fgPkg: String? = null
          Log.d("AppMonitorService", "Pulling app...")
          while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
              fgPkg = ev.packageName
            }
          }
          fgPkg?.let { pkg ->
            Log.d("AppMonitorService", "Foreground = $pkg")
            //if (shouldBlock(pkg)) {
            if (pkg != null && pkg != lastPkg && shouldBlock(pkg)) {
              Log.d("AppMonitorService", "shouldBlock inside = $pkg")
              sendBlockNotification(pkg)
            }
            lastPkg = pkg
          }

          Thread.sleep(1000)
        } catch (t: Throwable) {
          Log.e("AppMonitorService", "Loop error", t)
        }
      }
    }.start()
  }

  private fun shouldBlock(pkg: String): Boolean {
    val rulesJson = AppUtilsModule.currentRulesJson
    Log.e("AppMonitorService", "shouldBlock inside rulesJson = ${rulesJson}")
    val map: Map<String, Any> =
      Gson().fromJson(rulesJson, Map::class.java) as Map<String, Any>
    val pkgRule = map[pkg] as? Map<*, *> ?: return false
    Log.e("AppMonitorService", "shouldBlock inside pkgRule = ${pkgRule}")
    // 3) If it’s in the map *but* empty → trigger the initial setup
    if (pkgRule.isEmpty()) {
      Log.d("AppMonitorService", "First time seeing $pkg, showing setup")
      return true
    }
    // now check pkgRule["accessEnd"] and pkgRule["lockEnd"]
    val now = System.currentTimeMillis()
    val accessEnd = (pkgRule["accessEnd"] as? Double)?.toLong() ?: 0L
    val lockEnd   = (pkgRule["lockEnd"]   as? Double)?.toLong() ?: 0L
    Log.e("AppMonitorService", "shouldBlock inside accessEnd = ${accessEnd}")
    Log.e("AppMonitorService", "shouldBlock inside lockEnd = ${lockEnd}")
    return now > accessEnd && now < lockEnd
  }

  private fun sendBlockNotification(pkg: String) {
    Log.d("AppMonitorService", "sendBlockNotification inside = $pkg")
    val notificationId = NOTIF_ID_BLOCK_BASE  // always same ID for same channel
    val nm = getSystemService(NotificationManager::class.java)
    nm.notify(notificationId, buildBlockNotification(pkg))
    //nm.notify(NOTIF_ID_BLOCK_BASE + blockCount, buildBlockNotification(pkg))
  }

  override fun onDestroy() {
    running = false
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
