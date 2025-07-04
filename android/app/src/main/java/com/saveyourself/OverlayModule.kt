package com.saveyourself

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

// OverlayModule is now primarily for LockScreenActivity to use if RN context is ready
// It's no longer directly called by AppMonitorService for showing the overlay
class OverlayModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private var reactRootView: ReactRootView? = null
    private var windowManager: WindowManager? = null

    override fun getName(): String = "OverlayModule"

    init {
        windowManager = reactContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    // This method is now intended to be called by LockScreenActivity, NOT AppMonitorService directly
    @ReactMethod
    fun showLockScreen(packageName: String) {
        // This permission check is less critical here if LockScreenActivity handles it,
        // but good to keep as a safeguard.
        if (!Settings.canDrawOverlays(reactApplicationContext)) {
            Log.e("OverlayModule", "Overlay permission not granted. Cannot show lock screen.")
            return
        }

        if (reactRootView != null) {
            Log.d("OverlayModule", "Existing overlay found, hiding before showing new one.")
            hideLockScreen()
        }

        val reactInstanceManager = MainApplication.reactInstanceManager
        if (reactInstanceManager == null) {
            Log.e("OverlayModule", "ReactInstanceManager is null. Cannot show overlay.")
            return
        }

        reactRootView = ReactRootView(reactApplicationContext)
        val initialProps = Bundle()
        initialProps.putString("packageName", packageName)
        reactRootView?.startReactApplication(reactInstanceManager, "SetupOverlay", initialProps)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager?.addView(reactRootView, layoutParams)
            Log.d("OverlayModule", "Overlay shown for package: $packageName")
        } catch (e: Exception) {
            Log.e("OverlayModule", "Error showing overlay: ${e.message}", e)
        }
    }

    @ReactMethod
    fun hideLockScreen() {
        if (reactRootView != null) {
            try {
                windowManager?.removeView(reactRootView)
                reactRootView?.unmountReactApplication()
                reactRootView = null
                Log.d("OverlayModule", "Overlay hidden.")
            } catch (e: Exception) {
                Log.e("OverlayModule", "Error hiding overlay: ${e.message}", e)
            }
        } else {
            Log.d("OverlayModule", "No overlay to hide.")
        }
    }

    override fun invalidate() {
        super.invalidate()
        hideLockScreen()
    }
}
