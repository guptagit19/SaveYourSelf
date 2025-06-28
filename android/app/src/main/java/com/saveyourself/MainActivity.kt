package com.saveyourself

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
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

  override fun getMainComponentName(): String = "SaveYourSelf"
}
