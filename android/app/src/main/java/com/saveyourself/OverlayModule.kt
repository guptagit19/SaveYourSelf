package com.saveyourself

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle // Import Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.Arguments // Keep this import if you use other Arguments methods elsewhere
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class OverlayModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private var reactRootView: ReactRootView? = null
    private var windowManager: WindowManager? = null

    override fun getName(): String = "OverlayModule"

    init {
        windowManager = reactContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    @ReactMethod
    fun showLockScreen(packageName: String) {
        // Ensure we have the permission
        if (!Settings.canDrawOverlays(reactApplicationContext)) {
            Log.e("OverlayModule", "Overlay permission not granted. Cannot show lock screen.")
            return
        }

        // If an overlay is already showing, hide it first to prevent duplicates
        if (reactRootView != null) {
            Log.d("OverlayModule", "Existing overlay found, hiding before showing new one.")
            hideLockScreen()
        }

        // Get the ReactInstanceManager from the MainApplication's static reference
        val reactInstanceManager = MainApplication.reactInstanceManager
        if (reactInstanceManager == null) {
            Log.e("OverlayModule", "ReactInstanceManager is null. Cannot show overlay.")
            return
        }

        // Create a new ReactRootView
        reactRootView = ReactRootView(reactApplicationContext)
        // Fixed: Use Bundle() directly to create the bundle
        val initialProps = Bundle()
        initialProps.putString("packageName", packageName)
        // Set the component name that this ReactRootView will render
        reactRootView?.startReactApplication(reactInstanceManager, "SetupOverlay", initialProps)

        // Define layout parameters for the overlay window
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, // Width to match parent
            WindowManager.LayoutParams.MATCH_PARENT, // Height to match parent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY // Use this type for Android O and above
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE // Deprecated but works for older versions
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or // Capture all touches on the overlay
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or // Make it full screen
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or // Allow it to extend into screen decorations
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // Allow it to go beyond screen bounds
            PixelFormat.TRANSLUCENT // Transparent background
        )

        // Set gravity to fill the screen
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
                reactRootView?.unmountReactApplication() // Clean up React Native view
                reactRootView = null
                Log.d("OverlayModule", "Overlay hidden.")
            } catch (e: Exception) {
                Log.e("OverlayModule", "Error hiding overlay: ${e.message}", e)
            }
        } else {
            Log.d("OverlayModule", "No overlay to hide.")
        }
    }

    // Fixed: Replaced onCatalystInstanceDestroy() with invalidate()
    override fun invalidate() {
        super.invalidate()
        // Ensure overlay is hidden if React Native instance is destroyed
        hideLockScreen()
    }
}
