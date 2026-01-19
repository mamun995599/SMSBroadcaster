package com.smsbroadcaster.http

import android.util.Log
import com.smsbroadcaster.model.SMSMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class HttpClient(
    private val listener: HttpClientListener
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "HttpClient"
    }

    interface HttpClientListener {
        fun onRequestSuccess(message: String)
        fun onRequestFailure(error: String)
    }

    suspend fun sendSMS(
        ipAddress: String,
        port: Int,
        method: String,
        smsMessage: SMSMessage
    ) = withContext(Dispatchers.IO) {
        if (ipAddress.isBlank()) {
            Log.d(TAG, "HTTP target not configured, skipping")
            return@withContext
        }

        val baseUrl = "http://$ipAddress:$port/sms"

        try {
            val request = when (method.uppercase()) {
                "GET" -> createGetRequest(baseUrl, smsMessage)
                "POST" -> createPostRequest(baseUrl, smsMessage)
                else -> createGetRequest(baseUrl, smsMessage)
            }

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val successMsg = "[${getTimestamp()}] ✓ $method $baseUrl - ${response.code}"
                Log.d(TAG, successMsg)
                listener.onRequestSuccess(successMsg)
            } else {
                val errorMsg = "[${getTimestamp()}] ✗ $method $baseUrl - ${response.code} ${response.message}"
                Log.e(TAG, errorMsg)
                listener.onRequestFailure(errorMsg)
            }
            response.close()
        } catch (e: Exception) {
            val errorMsg = "[${getTimestamp()}] ✗ $method $baseUrl - ${e.message}"
            Log.e(TAG, errorMsg)
            listener.onRequestFailure(errorMsg)
        }
    }

    private fun createGetRequest(baseUrl: String, smsMessage: SMSMessage): Request {
        val encodedSender = URLEncoder.encode(smsMessage.sender, "UTF-8")
        val encodedMessage = URLEncoder.encode(smsMessage.message, "UTF-8")
        val url = "$baseUrl?sender=$encodedSender&message=$encodedMessage&timestamp=${smsMessage.timestamp}"

        return Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")
            .build()
    }

    private fun createPostRequest(baseUrl: String, smsMessage: SMSMessage): Request {
        val json = smsMessage.toJson()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        return Request.Builder()
            .url(baseUrl)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
    }

    private fun getTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}