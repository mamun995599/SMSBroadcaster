package com.smsbroadcaster.model

data class ConnectedClient(
    val id: String,
    val ipAddress: String,
    val connectedAt: Long
)