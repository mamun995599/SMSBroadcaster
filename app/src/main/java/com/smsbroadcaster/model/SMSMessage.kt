package com.smsbroadcaster.model

import com.google.gson.Gson

data class SMSMessage(
    val sender: String,
    val message: String,
    val timestamp: Long,
    val simSlot: Int = -1
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): SMSMessage = Gson().fromJson(json, SMSMessage::class.java)
    }
}