package com.saveyourself

import android.Manifest
import android.app.AlertDialog // Added import for AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri // Added import for Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.facebook.react.ReactActivity

class MainActivity : ReactActivity() {
  private val USAGE_STATS_REQUEST = 123
  private val NOTIF_REQUEST        = 456

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // 1) Usage-stats permission
    if (!hasUsageStatsPermission()) {
      startActivityForResult(
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
        USAGE_STATS_REQUEST
      )
    }

    // 2) Android 13+ notification permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        NOTIF_REQUEST
      )
    }

    // 3) Overlay permission
    ensureOverlayPermission()
  }

  private fun hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      android.os.Process.myUid(),
      packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
  }

    private fun ensureOverlayPermission() {
    if (!Settings.canDrawOverlays(this)) {
      AlertDialog.Builder(this)
        .setTitle("Overlay Permission Required")
        .setMessage("Please allow this app to draw over other apps so we can show the lock screen.")
        .setPositiveButton("Grant") { _, _ -> // Types inferred correctly with AlertDialog import
          startActivity(
            Intent(
              Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:$packageName")
            )
          )
        }
        .setCancelable(false)
        .show()
    }
  }

  override fun getMainComponentName(): String = "SaveYourSelf"
}
