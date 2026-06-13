package com.galynte.tapblok

import android.app.AppOpsManager
import android.app.Service
import android.content.Context
import android.os.Process

// A helper function to check if the monitoring service is currently running.
// The implementation uses a SharedPreferences flag that the service itself manages
// on start/stop (via onStartCommand / onDestroy). This avoids the deprecated
// ActivityManager.getRunningServices() API while keeping the exact same call sites
// and signature for minimal diff.
fun isServiceRunning(context: Context, serviceClass: Class<out Service>): Boolean {
    // serviceClass param is unused but kept for source compatibility with existing call sites.
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("is_monitoring_active", false)
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}