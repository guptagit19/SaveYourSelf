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
        private const val CHANNEL_ID_BLOCK   = "app_block_channel" // New channel for blocking notifications
        private const val NOTIF_ID_MONITOR   = 1
        private const val NOTIF_ID_BLOCK     = 2 // New notification ID for blocking
    }

    private lateinit var usageStats: UsageStatsManager
    private var running = true
    private var lastPkg: String? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannels() // Create both channels
        startForeground(NOTIF_ID_MONITOR, buildMonitorNotification())
        monitorLoop()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for the foreground service notification
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID_MONITOR, "App Monitor", NotificationManager.IMPORTANCE_LOW)
            )
            // Channel for the app blocking notification (higher importance)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID_BLOCK, "App Blocked Alert", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications for blocked applications."
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
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
                        if (pkg != lastPkg) {
                            Log.d("AppMonitorService", "Foreground = $pkg")
                            if (shouldBlock(pkg)) {
                                Log.d("AppMonitorService", "shouldBlock inside = $pkg")
                                // Now, show a notification that launches LockScreenActivity
                                showBlockingNotification(pkg)
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
        if (pkg == packageName ||
            pkg == "com.google.android.launcher" ||
            pkg == "com.android.launcher3" ||
            pkg == "com.android.systemui" ||
            pkg == "com.google.android.gms" ||
            pkg == "android"
        ) {
            return false
        }

        val rulesJson = AppUtilsModule.currentRulesJson
        val map: Map<String, Any> =
            Gson().fromJson(rulesJson, Map::class.java) as Map<String, Any>
        val pkgRule = map[pkg] as? Map<*, *> ?: return false
        if (pkgRule.isEmpty()) {
            Log.d("AppMonitorService", "First time seeing $pkg, showing setup")
            return true
        }
        val now = System.currentTimeMillis()
        val accessEnd = (pkgRule["accessEnd"] as? Double)?.toLong() ?: 0L
        val lockEnd    = (pkgRule["lockEnd"]    as? Double)?.toLong() ?: 0L
        return now > accessEnd && now < lockEnd
    }

    // NEW: Function to show a notification that launches LockScreenActivity
    private fun showBlockingNotification(pkg: String) {
        Log.d("AppMonitorService", "Showing blocking notification for $pkg")

        val intent = Intent(this, LockScreenActivity::class.java).apply {
            putExtra(LockScreenActivity.EXTRA_PACKAGE_NAME, pkg)
            // Add flags to ensure it launches correctly from a notification
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code, can be 0 or unique per notification
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_BLOCK)
            .setContentTitle("App Blocked!")
            .setContentText("$pkg is currently blocked. Tap to manage access.")
            .setSmallIcon(R.mipmap.ic_launcher) // Use your app's icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL) // High priority for lock screen-like behavior
            .setAutoCancel(true) // Dismiss notification when tapped
            .setFullScreenIntent(pendingIntent, true) // This is for full-screen notification on some devices
            .setContentIntent(pendingIntent) // Standard content intent when tapped
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID_BLOCK, notification)
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
