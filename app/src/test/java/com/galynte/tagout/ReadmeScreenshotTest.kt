/*
 * Copyright 2026 Galynte
 *
 * This file is part of Tagout, a fork of TapBlok by cajdata.
 * Original: https://github.com/cajdata/TapBlok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.galynte.tagout

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Process
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.galynte.tagout.ui.theme.TagoutTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSettings

/**
 * Renders the README screens from the real Compose UI. Output lands in
 * build/screenshots so the images can be copied into docs/assets/images.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h914dp-420dpi")
class ReadmeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun privacyNotice() {
        composeRule.setContent {
            TagoutTheme {
                PrivacyScreen(onAccept = {})
            }
        }
        composeRule.onRoot().save("app_privacy_notice.png")
    }

    @Test
    fun homeMonitoringActive() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_monitoring_active", true)
            .putInt("blocked_app_attempts", 0)
            .putBoolean("enable_qr_code", false)
            .commit()
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        shadowOf(appOps).setMode(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
            AppOpsManager.MODE_ALLOWED
        )
        ShadowSettings.setCanDrawOverlays(true)

        composeRule.setContent {
            TagoutTheme {
                MainScreen()
            }
        }
        composeRule.onRoot().save("app_active.png")
    }

    @Test
    fun settings() {
        composeRule.setContent {
            TagoutTheme {
                SettingsScreen(
                    onBackClick = {},
                    isMonitoringActive = false,
                    enableQrCode = false,
                    breakDurationMinutes = 0,
                    monitoringDurationMinutes = 0,
                    onEnableQrCodeChanged = {},
                    onBreakOptionChanged = {},
                    onMonitoringDurationChanged = {},
                    onWriteNfcTagClick = {},
                    onShowQrCodeClick = {},
                    onEditPermissionsClick = {}
                )
            }
        }
        composeRule.onRoot().save("app_settings.png")
    }

    @Test
    fun blockedApp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notes = ApplicationInfo().apply {
            packageName = "com.example.notes"
            nonLocalizedLabel = "Notes"
        }
        val packageInfo = PackageInfo().apply {
            packageName = notes.packageName
            applicationInfo = notes
        }
        val packageManager = shadowOf(context.packageManager)
        packageManager.installPackage(packageInfo)
        packageManager.setApplicationIcon("com.example.notes", notesIcon(context))

        composeRule.setContent {
            TagoutTheme {
                BlockingScreen(
                    packageName = "com.example.notes",
                    onGoHomeClick = {},
                    onTakeBreakClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().save("app_blocked.png")
    }

    private fun shot(name: String) = java.io.File("build/screenshots", name).apply {
        parentFile?.mkdirs()
    }

    private fun notesIcon(context: Context): BitmapDrawable {
        val bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF3E5C73.toInt() }
        canvas.drawRoundRect(8f, 8f, 184f, 184f, 40f, 40f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(56f, 44f, 136f, 148f, 14f, 14f, paint)
        paint.color = 0xFF3E5C73.toInt()
        canvas.drawRoundRect(72f, 68f, 120f, 80f, 4f, 4f, paint)
        canvas.drawRoundRect(72f, 92f, 120f, 104f, 4f, 4f, paint)
        canvas.drawRoundRect(72f, 116f, 104f, 128f, 4f, 4f, paint)
        return BitmapDrawable(context.resources, bitmap)
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun SemanticsNodeInteraction.save(name: String) {
        captureRoboImage(
            shot(name),
            RoborazziOptions(taskType = RoborazziTaskType.Record)
        )
    }
}
