/*
 * Copyright 2026 Galynte
 *
 * This file is part of TapOut, a fork of TapBlok by cajdata.
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

package com.galynte.tapblok

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Android 17 refuses to dispatch an NFC tag to an activity that is not guarded by
 * android.permission.DISPATCH_NFC_MESSAGE when the app targets a newer SDK than Android 16.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NfcHandlerDispatchTest {

    @Test
    fun nfcHandlerRequiresNfcServicePermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = context.packageManager.getActivityInfo(
            ComponentName(context, NfcHandlerActivity::class.java),
            PackageManager.GET_META_DATA
        )
        assertEquals("android.permission.DISPATCH_NFC_MESSAGE", info.permission)
    }
}
