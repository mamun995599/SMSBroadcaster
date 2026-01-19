package com.smsbroadcaster.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {

    fun getDeviceIpAddresses(): List<String> {
        val ipAddresses = mutableListOf<String>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        address.hostAddress?.let { ipAddresses.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ipAddresses
    }

    fun getWifiIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            return if (ipInt != 0) {
                String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getAllIpAddressesFormatted(): String {
        val ips = getDeviceIpAddresses()
        return if (ips.isEmpty()) {
            "No network connection"
        } else {
            ips.joinToString("\n")
        }
    }
}