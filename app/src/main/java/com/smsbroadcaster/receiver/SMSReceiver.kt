package com.smsbroadcaster.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.smsbroadcaster.model.SMSMessage
import com.smsbroadcaster.service.SMSBroadcastService

class SMSReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SMSReceiver"
        const val ACTION_SMS_RECEIVED = "com.smsbroadcaster.SMS_RECEIVED"
        const val EXTRA_SMS_MESSAGE = "sms_message"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        context?.let { ctx ->
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            messages?.forEach { smsMessage ->
                val sender = smsMessage.displayOriginatingAddress ?: "Unknown"
                val body = smsMessage.messageBody ?: ""
                val timestamp = smsMessage.timestampMillis

                Log.d(TAG, "SMS received from: $sender")

                val sms = SMSMessage(
                    sender = sender,
                    message = body,
                    timestamp = timestamp
                )

                // Send to service
                val serviceIntent = Intent(ctx, SMSBroadcastService::class.java).apply {
                    action = ACTION_SMS_RECEIVED
                    putExtra(EXTRA_SMS_MESSAGE, sms.toJson())
                }
                ctx.startService(serviceIntent)
            }
        }
    }
}