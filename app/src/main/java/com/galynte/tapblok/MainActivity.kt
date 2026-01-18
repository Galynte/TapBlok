package com.galynte.tapblok

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.galynte.tapblok.ui.theme.TapBlokTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TapBlokTheme {
                MainScreen()
            }
        }
    }
}
private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
private const val PREF_ENABLE_QR_CODE = "enable_qr_code"
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isServiceRunning by remember { mutableStateOf(isServiceRunning(context, AppMonitoringService::class.java)) }
    var blockedAppAttempts by remember { mutableStateOf(0) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var enableQrCode by remember { mutableStateOf(prefs.getBoolean(PREF_ENABLE_QR_CODE, false)) }
    var holdProgress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    // NEW: Session duration & remaining time (calculated only on resume)
    var sessionDurationMinutes by remember { mutableStateOf(0) }
    var remainingMinutes by remember { mutableStateOf(0) }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasUsagePermission = hasUsageStatsPermission(context)
        canDrawOverlays = Settings.canDrawOverlays(context)
        isServiceRunning = isServiceRunning(context, AppMonitoringService::class.java)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    val qrCodeScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents == QrCodeActivity.QR_CODE_CONTENT) {
            val serviceIntent = Intent(context, AppMonitoringService::class.java)
            if (isServiceRunning) {
                context.stopService(serviceIntent)
                Toast.makeText(context, "Monitoring stopped.", Toast.LENGTH_SHORT).show()
                isServiceRunning = false
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Toast.makeText(context, "Monitoring started.", Toast.LENGTH_SHORT).show()
                isServiceRunning = true
            }
        } else if (result.contents != null) {
            Toast.makeText(context, "Incorrect QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    // Refresh state & calculate remaining time when activity resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isServiceRunning = isServiceRunning(context, AppMonitoringService::class.java)
                val prefsNow = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                blockedAppAttempts = prefsNow.getInt("blocked_app_attempts", 0)
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                enableQrCode = prefsNow.getBoolean(PREF_ENABLE_QR_CODE, false)

                // Calculate session info
                sessionDurationMinutes = prefsNow.getInt("monitoring_duration_minutes", 0)
                if (isServiceRunning && sessionDurationMinutes > 0) {
                    val startMs = prefsNow.getLong("session_start_timestamp", 0L)
                    if (startMs > 0) {
                        // normal calculation
                        val elapsedMs = System.currentTimeMillis() - startMs
                        val totalMs = sessionDurationMinutes * 60_000L
                        val remainingMs = (totalMs - elapsedMs).coerceAtLeast(0)
                        remainingMinutes = (remainingMs / 60_000L).toInt().coerceAtLeast(0)
                    } else {
                        // Fallback when timestamp not yet written (e.g. very first start)
                        remainingMinutes = sessionDurationMinutes // optimistic: show full duration
                    }
                } else {
                    remainingMinutes = 0
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            val duration = 60000L // 60 seconds
            while (isHolding && System.currentTimeMillis() - startTime < duration) {
                holdProgress = (System.currentTimeMillis() - startTime) / duration.toFloat()
                delay(50)
            }
            if (isHolding) {
                holdProgress = 1f
                val serviceIntent = Intent(context, AppMonitoringService::class.java)
                context.stopService(serviceIntent)
                isServiceRunning = false
            }
        } else {
            holdProgress = 0f
        }
    }

    val allPermissionsGranted = hasUsagePermission && canDrawOverlays

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tapblok_logo),
                contentDescription = "TapBlok Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )
            Text(
                text = "TapBlok",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (allPermissionsGranted) {
                Text(
                    text = if (isServiceRunning) "Monitoring is Active" else "Monitoring is Inactive",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isServiceRunning) Color(0xFF4CAF50) else Color.Gray
                )

                if (isServiceRunning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Blocked App Attempts: $blockedAppAttempts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    // NEW: Show session duration / remaining time
                    if (sessionDurationMinutes > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (remainingMinutes > 0) {
                                "Session auto-ends in $remainingMinutes minutes"
                            } else {
                                "Session ending soon"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Session is set to infinite duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val serviceIntent = Intent(context, AppMonitoringService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        isServiceRunning = true
                    },
                    enabled = !isServiceRunning
                ) {
                    Text("Start Monitoring")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        context.startActivity(Intent(context, AppSelectionActivity::class.java))
                    },
                    enabled = !isServiceRunning
                ) {
                    Text("Manage Blocked Apps")
                }
                if (enableQrCode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (hasCameraPermission) {
                            val options = ScanOptions().setOrientationLocked(true)
                            qrCodeScannerLauncher.launch(options)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Scan QR Code")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        intent.putExtra("is_monitoring_active", isServiceRunning)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Settings")
                }

                if (isServiceRunning) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Press and hold for 60 seconds to force stop",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHolding = true
                                        tryAwaitRelease()
                                        isHolding = false
                                    }
                                )
                            }
                    ) {
                        LinearProgressIndicator(
                            progress = { holdProgress },
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "EMERGENCY STOP",
                            modifier = Modifier.align(Alignment.Center),
                            color = if (holdProgress > 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Text(
                    text = "Please grant the required permissions to use TapBlok.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (!hasUsagePermission) {
                    Button(onClick = {
                        settingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }) {
                        Text("Grant Usage Access")
                    }
                }
                if (!canDrawOverlays) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        settingsLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    }) {
                        Text("Grant Overlay Permission")
                    }
                }
            }
        }
    }
}