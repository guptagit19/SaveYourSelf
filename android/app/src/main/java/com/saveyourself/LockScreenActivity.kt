package com.saveyourself

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.util.Log
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler

// This activity will be launched by the AppMonitorService immediately
class LockScreenActivity : Activity(), DefaultHardwareBackBtnHandler {

    private var reactRootView: ReactRootView? = null
    private var packageName: String? = null

    companion object {
        const val EXTRA_PACKAGE_NAME = "packageName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        Log.d("LockScreenActivity", "onCreate: Launched for package: $packageName")

        // Try to load React Native UI first
        if (MainApplication.reactInstanceManager != null && MainApplication.reactInstanceManager?.currentReactContext?.hasActiveCatalystInstance() == true) {
            Log.d("LockScreenActivity", "React Native context active, attempting to load RN UI.")
            loadReactNativeUI()
        } else {
            // Fallback to native UI if React Native context is not ready
            Log.w("LockScreenActivity", "React Native context not active, falling back to native UI.")
            loadNativeUI()
        }
    }

    private fun loadReactNativeUI() {
        reactRootView = ReactRootView(this)
        val initialProps = Bundle()
        initialProps.putString("packageName", packageName)
        reactRootView?.startReactApplication(
            MainApplication.reactInstanceManager,
            "SetupOverlay", // This should match AppRegistry.registerComponent in SetupOverlayRoot.js
            initialProps
        )
        setContentView(reactRootView)
    }

    private fun loadNativeUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#111111")) // Dark background
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val appNameTextView = TextView(this).apply {
            text = "App Blocked: ${packageName ?: "Unknown App"}"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32 // Margin below text
            }
        }

        val messageTextView = TextView(this).apply {
            text = "Please return to SaveYourSelf to manage access."
            textSize = 16f
            setTextColor(Color.parseColor("#CCCCCC")) // Lighter text color
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 64 // Margin below text
            }
        }

        val openAppButton = Button(this).apply {
            text = "Open SaveYourSelf"
            setBackgroundColor(Color.parseColor("#28a745")) // Green button
            setTextColor(Color.WHITE)
            setOnClickListener {
                // Launch your main app
                val intent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish() // Finish this lock screen activity
            }
        }

        layout.addView(appNameTextView)
        layout.addView(messageTextView)
        layout.addView(openAppButton)

        setContentView(layout)
    }

    override fun onPause() {
        super.onPause()
        // If the user navigates away from this LockScreenActivity,
        // we might want to ensure it's still active or re-launched if needed.
        // For now, let's keep it simple.
    }

    override fun onDestroy() {
        super.onDestroy()
        reactRootView?.unmountReactApplication() // Clean up RN view
        Log.d("LockScreenActivity", "onDestroy: LockScreenActivity destroyed.")
    }

    // Required for React Native's back button handling
    override fun invokeDefaultOnBackPressed() {
        Log.d("LockScreenActivity", "Back button pressed on LockScreenActivity.")
        // To always go back to your main app:
        val intent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
