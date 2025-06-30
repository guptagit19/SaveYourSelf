package com.saveyourself

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate


class SetupActivity : ReactActivity() {
  override fun getMainComponentName(): String = "SetupOverlay"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("SetupActivity", "onCreate → SetupActivity launched with pkg=${intent.getStringExtra("pkg")}")
  }

  override fun onStart() {
    super.onStart()
    Log.d("SetupActivity", "onStart → SetupActivity visible")
  }

  override fun onResume() {
    super.onResume()
    Log.d("SetupActivity", "onResume → SetupActivity resumed")
  }

  // <-- add this:
  override fun createReactActivityDelegate(): ReactActivityDelegate {
    return object : ReactActivityDelegate(this, getMainComponentName()) {
      // this is where you pass initialProps into RN
      override fun getLaunchOptions(): Bundle? {
        val pkg = intent.getStringExtra("pkg")
        Log.d("SetupActivity", "getLaunchOptions → pkg=$pkg")
        return Bundle().apply {
          putString("packageName", pkg)
        }
      }
    }
  }

  
}