package com.smsbroadcaster.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smsbroadcaster.service.SMSBroadcastService
import com.smsbroadcaster.utils.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {

            context?.let { ctx ->
                val prefManager = PreferenceManager(ctx)
                if (prefManager.isServerEnabled) {
                    Log.d(TAG, "Boot completed, restarting SMS Broadcaster service")
                    val serviceIntent = Intent(ctx, SMSBroadcastService::class.java).apply {
                        action = SMSBroadcastService.ACTION_START_SERVICE
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ctx.startForegroundService(serviceIntent)
                    } else {
                        ctx.startService(serviceIntent)
                    }
                }
            }
        }
    }
}