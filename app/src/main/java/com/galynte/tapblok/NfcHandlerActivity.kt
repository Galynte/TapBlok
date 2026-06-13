/*
 * Copyright 2025-2026 Galynte
 *
 * This file is part of TapBlok, a fork of the original project by cajdata.
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

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.IntentCompat
// ADD THIS IMPORT STATEMENT
import com.galynte.tapblok.isServiceRunning

class NfcHandlerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("NfcHandlerActivity", "Activity launched by NFC intent.")
        handleNfcIntent()
    }

    private fun handleNfcIntent() {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val messages = IntentCompat.getParcelableArrayExtra(intent, NfcAdapter.EXTRA_NDEF_MESSAGES, Parcelable::class.java)
                ?.mapNotNull { it as? NdefMessage }
                ?.toTypedArray()
            if (!messages.isNullOrEmpty()) {
                val ndefMessage = messages[0]
                val record = ndefMessage.records[0]
                val payload = String(record.payload)

                Log.d("NfcHandlerActivity", "NFC Tag Payload: $payload")

                val serviceIntent = Intent(this, AppMonitoringService::class.java)

                if (isServiceRunning(this, AppMonitoringService::class.java)) {
                    // If the service is running, stop it.
                    stopService(serviceIntent)
                    Toast.makeText(this, "Monitoring stopped.", Toast.LENGTH_SHORT).show()
                } else {
                    // If the service is not running, start it.
                    startForegroundService(serviceIntent)
                    Toast.makeText(this, "Monitoring started.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Finish the activity immediately since it has no UI
        finish()
    }
}