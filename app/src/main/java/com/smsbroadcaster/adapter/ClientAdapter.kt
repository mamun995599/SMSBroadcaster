package com.smsbroadcaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbroadcaster.R
import com.smsbroadcaster.model.ConnectedClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientAdapter : ListAdapter<ConnectedClient, ClientAdapter.ClientViewHolder>(ClientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClientIp: TextView = itemView.findViewById(R.id.tvClientIp)
        private val tvClientInfo: TextView = itemView.findViewById(R.id.tvClientInfo)

        fun bind(client: ConnectedClient) {
            tvClientIp.text = client.ipAddress
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val connectedTime = sdf.format(Date(client.connectedAt))
            tvClientInfo.text = "Connected at $connectedTime"
        }
    }

    class ClientDiffCallback : DiffUtil.ItemCallback<ConnectedClient>() {
        override fun areItemsTheSame(oldItem: ConnectedClient, newItem: ConnectedClient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConnectedClient, newItem: ConnectedClient): Boolean {
            return oldItem == newItem
        }
    }
}