package com.smsbroadcaster.websocket

import android.util.Log
import com.smsbroadcaster.model.ConnectedClient
import com.smsbroadcaster.model.SMSMessage
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class SMSWebSocketServer(
    port: Int,
    private val listener: WebSocketServerListener
) : WebSocketServer(InetSocketAddress("0.0.0.0", port)) {

    private val connectedClients = ConcurrentHashMap<String, ConnectedClient>()

    companion object {
        private const val TAG = "SMSWebSocketServer"
    }

    interface WebSocketServerListener {
        fun onClientConnected(client: ConnectedClient)
        fun onClientDisconnected(clientId: String)
        fun onServerStarted(port: Int)
        fun onServerStopped()
        fun onServerError(error: String)
        fun onClientsUpdated(clients: List<ConnectedClient>)
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.let { socket ->
            val clientIp = socket.remoteSocketAddress?.address?.hostAddress ?: "Unknown"
            val clientId = "${clientIp}:${socket.remoteSocketAddress?.port}"
            val client = ConnectedClient(
                id = clientId,
                ipAddress = clientIp,
                connectedAt = System.currentTimeMillis()
            )
            connectedClients[clientId] = client
            Log.d(TAG, "Client connected: $clientIp")
            listener.onClientConnected(client)
            listener.onClientsUpdated(connectedClients.values.toList())

            // Send welcome message
            socket.send("""{"type":"welcome","message":"Connected to SMS Broadcaster"}""")
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        conn?.let { socket ->
            val clientIp = socket.remoteSocketAddress?.address?.hostAddress ?: "Unknown"
            val clientId = "${clientIp}:${socket.remoteSocketAddress?.port}"
            connectedClients.remove(clientId)
            Log.d(TAG, "Client disconnected: $clientIp")
            listener.onClientDisconnected(clientId)
            listener.onClientsUpdated(connectedClients.values.toList())
        }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Log.d(TAG, "Message received: $message")
        // Echo back or handle commands if needed
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.e(TAG, "WebSocket error: ${ex?.message}")
        listener.onServerError(ex?.message ?: "Unknown error")
    }

    override fun onStart() {
        Log.d(TAG, "WebSocket server started on port ${this.port}")
        listener.onServerStarted(this.port)
    }

    fun broadcastSMS(smsMessage: SMSMessage) {
        val json = smsMessage.toJson()
        val wrappedMessage = """{"type":"sms","data":$json}"""
        broadcast(wrappedMessage)
        Log.d(TAG, "Broadcasted SMS to ${connectedClients.size} clients")
    }

    fun getConnectedClients(): List<ConnectedClient> {
        return connectedClients.values.toList()
    }

    fun getClientCount(): Int {
        return connectedClients.size
    }

    fun stopServer() {
        try {
            stop(1000)
            connectedClients.clear()
            listener.onServerStopped()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
        }
    }
}