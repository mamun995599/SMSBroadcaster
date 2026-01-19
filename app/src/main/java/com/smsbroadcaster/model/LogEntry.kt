package com.smsbroadcaster.model

data class LogEntry(
    val timestamp: Long,
    val message: String,
    val isSuccess: Boolean,
    val type: LogType
)

enum class LogType {
    HTTP_REQUEST,
    HTTP_RESPONSE,
    WEBSOCKET,
    SMS,
    ERROR,
    INFO
}