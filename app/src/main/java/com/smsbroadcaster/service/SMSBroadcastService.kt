package com.smsbroadcaster.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smsbroadcaster.MainActivity
import com.smsbroadcaster.R
import com.smsbroadcaster.http.HttpClient
import com.smsbroadcaster.model.ConnectedClient
import com.smsbroadcaster.model.SMSMessage
import com.smsbroadcaster.receiver.SMSReceiver
import com.smsbroadcaster.utils.PreferenceManager
import com.smsbroadcaster.websocket.SMSWebSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SMSBroadcastService : Service(),
    SMSWebSocketServer.WebSocketServerListener,
    HttpClient.HttpClientListener {

    private val binder = LocalBinder()
    private var webSocketServer: SMSWebSocketServer? = null
    private lateinit var httpClient: HttpClient
    private lateinit var prefManager: PreferenceManager
    private var wakeLock: PowerManager.WakeLock? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "SMSBroadcastService"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "sms_broadcaster_channel"

        const val ACTION_START_SERVICE = "com.smsbroadcaster.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.smsbroadcaster.STOP_SERVICE"

        // Broadcast actions for UI updates
        const val ACTION_CLIENT_UPDATE = "com.smsbroadcaster.CLIENT_UPDATE"
        const val ACTION_LOG_UPDATE = "com.smsbroadcaster.LOG_UPDATE"
        const val ACTION_SERVER_STATUS = "com.smsbroadcaster.SERVER_STATUS"

        const val EXTRA_CLIENTS = "clients"
        const val EXTRA_LOG_MESSAGE = "log_message"
        const val EXTRA_LOG_SUCCESS = "log_success"
        const val EXTRA_SERVER_RUNNING = "server_running"
        const val EXTRA_PORT = "port"
    }

    inner class LocalBinder : Binder() {
        fun getService(): SMSBroadcastService = this@SMSBroadcastService
    }

    override fun onCreate() {
        super.onCreate()
        prefManager = PreferenceManager(this)
        httpClient = HttpClient(this)
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> startBroadcastServer()
            ACTION_STOP_SERVICE -> stopBroadcastServer()
            SMSReceiver.ACTION_SMS_RECEIVED -> {
                intent.getStringExtra(SMSReceiver.EXTRA_SMS_MESSAGE)?.let { json ->
                    val smsMessage = SMSMessage.fromJson(json)
                    handleIncomingSMS(smsMessage)
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SMSBroadcaster::WakeLock"
        ).apply {
            acquire()
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun startBroadcastServer() {
        val port = prefManager.webSocketPort.takeIf { it > 0 }
            ?: PreferenceManager.DEFAULT_WEBSOCKET_PORT

        try {
            webSocketServer?.stopServer()
            webSocketServer = SMSWebSocketServer(port, this)
            webSocketServer?.isReuseAddr = true
            webSocketServer?.start()

            startForeground(NOTIFICATION_ID, createNotification("Server running on port $port"))
            prefManager.isServerEnabled = true

            Log.d(TAG, "WebSocket server started on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server: ${e.message}")
            broadcastLog("Failed to start server: ${e.message}", false)
        }
    }

    private fun stopBroadcastServer() {
        webSocketServer?.stopServer()
        webSocketServer = null
        prefManager.isServerEnabled = false

        broadcastServerStatus(false, 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "WebSocket server stopped")
    }

    private fun handleIncomingSMS(smsMessage: SMSMessage) {
        Log.d(TAG, "Processing SMS from: ${smsMessage.sender}")

        // Broadcast via WebSocket
        webSocketServer?.broadcastSMS(smsMessage)
        broadcastLog("SMS received from ${smsMessage.sender}", true)

        // Send via HTTP if configured
        val httpIp = prefManager.httpIp
        val httpPort = prefManager.httpPort
        val httpMethod = prefManager.httpMethod

        if (httpIp.isNotBlank()) {
            serviceScope.launch {
                httpClient.sendSMS(httpIp, httpPort, httpMethod, smsMessage)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SMSBroadcastService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }

        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // WebSocket Server Listener implementations
    override fun onClientConnected(client: ConnectedClient) {
        broadcastClientUpdate()
        updateNotification("${getClientCount()} client(s) connected")
    }

    override fun onClientDisconnected(clientId: String) {
        broadcastClientUpdate()
        updateNotification("${getClientCount()} client(s) connected")
    }

    override fun onServerStarted(port: Int) {
        broadcastServerStatus(true, port)
        broadcastLog("WebSocket server started on port $port", true)
    }

    override fun onServerStopped() {
        broadcastServerStatus(false, 0)
        broadcastLog("WebSocket server stopped", true)
    }

    override fun onServerError(error: String) {
        broadcastLog("Server error: $error", false)
    }

    override fun onClientsUpdated(clients: List<ConnectedClient>) {
        broadcastClientUpdate()
    }

    // HTTP Client Listener implementations
    override fun onRequestSuccess(message: String) {
        broadcastLog(message, true)
    }

    override fun onRequestFailure(error: String) {
        broadcastLog(error, false)
    }

    // Broadcast helpers
    private fun broadcastClientUpdate() {
        val clients = webSocketServer?.getConnectedClients() ?: emptyList()
        val intent = Intent(ACTION_CLIENT_UPDATE).apply {
            putExtra(EXTRA_CLIENTS, ArrayList(clients.map { "${it.ipAddress}|${it.connectedAt}" }))
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLog(message: String, isSuccess: Boolean) {
        val intent = Intent(ACTION_LOG_UPDATE).apply {
            putExtra(EXTRA_LOG_MESSAGE, message)
            putExtra(EXTRA_LOG_SUCCESS, isSuccess)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastServerStatus(isRunning: Boolean, port: Int) {
        val intent = Intent(ACTION_SERVER_STATUS).apply {
            putExtra(EXTRA_SERVER_RUNNING, isRunning)
            putExtra(EXTRA_PORT, port)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun getConnectedClients(): List<ConnectedClient> {
        return webSocketServer?.getConnectedClients() ?: emptyList()
    }

    fun getClientCount(): Int {
        return webSocketServer?.getClientCount() ?: 0
    }

    fun isServerRunning(): Boolean {
        return webSocketServer != null
    }

    override fun onDestroy() {
        releaseWakeLock()
        webSocketServer?.stopServer()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Restart service if task is removed but server should be running
        if (prefManager.isServerEnabled) {
            val restartIntent = Intent(this, SMSBroadcastService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        }
        super.onTaskRemoved(rootIntent)
    }
}