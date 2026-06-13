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