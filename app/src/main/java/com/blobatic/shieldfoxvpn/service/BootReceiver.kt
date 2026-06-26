package com.blobatic.shieldfoxvpn.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Auto-connect on boot if user had it enabled
            // Read from DataStore prefs and start VPN service if needed
        }
    }
}
