package com.saveyourself

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Base64
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import android.util.Log

class AppUtilsModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  companion object {
    @JvmStatic
    var currentRulesJson: String = "{}"
  }

  private val gson = Gson()

  override fun getName(): String = "AppUtilsModule"

  @ReactMethod
  fun getInstalledApps(promise: Promise) {
    try {
      val pm = reactApplicationContext.packageManager
      val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        .map {
          // Render icon to Base64
          val icon = it.loadIcon(pm)
          val bmp = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
          val canvas = Canvas(bmp)
          icon.setBounds(0, 0, canvas.width, canvas.height)
          icon.draw(canvas)
          val baos = ByteArrayOutputStream().apply { bmp.compress(Bitmap.CompressFormat.PNG, 100, this) }

          mapOf(
            "name" to it.loadLabel(pm).toString(),
            "packageName" to it.packageName,
            "icon" to Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
          )
        }
      promise.resolve(gson.toJson(apps))
    } catch (e: Exception) {
      promise.reject("ERR_GET_APPS", e)
    }
  }

  @ReactMethod
  fun initAppMonitor(rulesJson: String) {
    Log.d("[AppUtilsModule]", "initAppMonitor → ${rulesJson}")
    currentRulesJson = rulesJson
    try {
      startAppMonitoring()
    } catch (e: SecurityException) {
      Log.d("[AppUtilsModule]", "Cannot start service, missing permission → ${e}")
    }
  }

  @ReactMethod
  fun updateAccessRules(rulesJson: String) {
    Log.d("[AppUtilsModule]", "updateAccessRules → ${rulesJson}")
    currentRulesJson = rulesJson
  }

  @ReactMethod
  fun startAppMonitoring() {
     Log.d("[AppUtilsModule]", "startAppMonitoring...")
    val intent = Intent(reactApplicationContext, AppMonitorService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      reactApplicationContext.startForegroundService(intent)
       Log.d("[AppUtilsModule]", "startAppMonitoring if..")
    } else {
      reactApplicationContext.startService(intent)
       Log.d("[AppUtilsModule]", "startAppMonitoring else ...")
    }
  }

  @ReactMethod
  fun stopAppMonitoring() {
    Log.d("[AppUtilsModule]", "stopAppMonitoring...")
    reactApplicationContext.stopService(
      Intent(reactApplicationContext, AppMonitorService::class.java)
    )
  }

  /** Call this from JS when you want to close the SetupActivity */
  @ReactMethod
  fun finishSetupActivity() {
    Log.d("[AppUtilsModule]", "finishSetupActivity...")
    currentActivity?.finish()
  }

  // no-op stubs for NativeEventEmitter
  @ReactMethod
  fun addListener(eventName: String) { /* no-op */ }

  @ReactMethod
  fun removeListeners(count: Double) { /* no-op */ }

  // helper to push any string back into JS
  private fun sendToJs(tag: String, msg: String) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(tag, msg)
  }
}
