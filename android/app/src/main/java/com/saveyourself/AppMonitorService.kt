package com.saveyourself

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.facebook.react.ReactApplication
import com.facebook.react.modules.core.DeviceEventManagerModule

class AppMonitorService : Service() {
  private lateinit var usageStats: UsageStatsManager
  private var running = true

  override fun onCreate() {
    super.onCreate()
    Log.d("[AppMonitorService]","Service Started")
    //sendToJs("NATIVE_LOG", "Service Started")
    usageStats = getSystemService(Context.USAGE_STATS_SERVICE)
        as UsageStatsManager

    startForeground(1, buildNotification())
    Log.d("[AppMonitorService]","Service created with rules=${AppUtilsModule.currentRulesJson}")
    monitorLoop()
  }

  private fun buildNotification(): Notification {
    val channelId = "app_monitor_channel"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val chan = NotificationChannel(channelId, "App Monitor", NotificationManager.IMPORTANCE_LOW)
      (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        .createNotificationChannel(chan)
      Log.d("[AppMonitorService]","Notification channel created")
    }
    return Notification.Builder(this, channelId)
      .setContentTitle("Focus Mode Active")
      .setContentText("Monitoring your app usage…")
      .setSmallIcon(R.mipmap.ic_launcher)
      .build()
  }

  private fun monitorLoop() {
    Thread {
      while (running) {
        try {
          val now = System.currentTimeMillis()
          val events = usageStats.queryEvents(now - 1000, now)
          val ev = UsageEvents.Event()
          var fgPkg: String? = null
          Log.d("[AppMonitorService]","Polling usage events - ${fgPkg}")
          while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
              fgPkg = ev.packageName
              Log.d("[AppMonitorService]","Foreground event: ${fgPkg}")
            }
          }
          fgPkg?.let {
            
            //emitForeground(it)
            //sendToJs("APP_IN_FOREGROUND", it)
          }

          Thread.sleep(1000)
        } catch (t: Throwable) {
          Log.d("[AppMonitorService]","Error in loop: ${t.message}")
        }
      }
    }.start()
  }

  private fun emitForeground(pkg: String) {
    Log.d("[AppMonitorService]","EMIT_FOREGROUND → $pkg")
    (application as ReactApplication)
      .reactNativeHost
      .reactInstanceManager
      .currentReactContext
      ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit("APP_IN_FOREGROUND", pkg)
  }

  override fun onDestroy() {
    running = false
    Log.d("[AppMonitorService]","Service destroyed")
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun sendToJs(eventName: String, message: String) {
    Log.d("[AppMonitorService]","sendToJs message = ${message}")
    (application as ReactApplication)
      .reactNativeHost
      .reactInstanceManager
      .currentReactContext
      ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit(eventName, message)
  }
}
