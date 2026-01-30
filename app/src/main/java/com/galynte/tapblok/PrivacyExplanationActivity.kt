package com.galynte.tapblok

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galynte.tapblok.ui.theme.TapBlokTheme

class PrivacyExplanationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("privacy_accepted", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            TapBlokTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrivacyScreen(
                        onAccept = {
                            prefs.edit().putBoolean("privacy_accepted", true).apply()
                            startActivity(Intent(this@PrivacyExplanationActivity, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyScreen(onAccept: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Your Privacy Matters",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "TapBlok needs special permissions to work, but we take your privacy seriously.",
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "What permissions we need and why:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionItem(
                        title = "App Usage Access",
                        description = "To detect when you're trying to open a blocked app during a focus session."
                    )
                    PermissionItem(
                        title = "Draw Over Other Apps",
                        description = "To temporarily block touches on blocked apps (no screenshots or reading content)."
                    )
                    PermissionItem(
                        title = "Camera (optional)",
                        description = "Only if you choose QR code scanning to start/end sessions."
                    )
                    PermissionItem(
                        title = "NFC (optional)",
                        description = "Only if you use NFC tags to control sessions."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Important:\n" +
                        "• TapBlok runs entirely on your device\n" +
                        "• No data is collected, stored remotely, or shared\n" +
                        "• We cannot see your screen content, passwords, or messages\n" +
                        "• You can revoke permissions anytime in Settings",
                fontSize = 16.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("I Understand – Continue", fontSize = 18.sp)
        }
    }
}

@Composable
fun PermissionItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}