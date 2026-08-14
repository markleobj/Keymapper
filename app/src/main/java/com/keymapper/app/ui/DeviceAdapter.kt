package com.keymapper.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.model.DeviceInfo
import com.keymapper.app.R
import com.keymapper.app.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onConnect: (String) -> Unit,
    private val onDisconnect: () -> Unit
) : ListAdapter<DeviceInfo, DeviceAdapter.VH>(DIFF) {

    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    private var connectedAddress: String? = null

    fun updateConnectionState(state: ConnectionState) {
        connectionState = state
        notifyDataSetChanged()
    }

    fun updateConnectedDevice(address: String?) {
        connectedAddress = address
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class VH(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeviceInfo) {
            binding.tvName.text = item.name ?: "未知设备"
            binding.tvAddress.text = item.address

            val isConnected = connectedAddress == item.address
            when {
                connectionState == ConnectionState.CONNECTING && isConnected -> {
                    binding.tvStatus.text = "连接中…"
                    binding.btnAction.text = "取消"
                    binding.btnAction.setOnClickListener { onDisconnect() }
                }
                isConnected && connectionState == ConnectionState.CONNECTED -> {
                    binding.tvStatus.text = "已连接"
                    binding.btnAction.text = "断开"
                    binding.btnAction.setOnClickListener { onDisconnect() }
                }
                else -> {
                    binding.tvStatus.text = ""
                    binding.btnAction.text = "连接"
                    binding.btnAction.setOnClickListener { onConnect(item.address) }
                }
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DeviceInfo>() {
            override fun areItemsTheSame(a: DeviceInfo, b: DeviceInfo) = a.address == b.address
            override fun areContentsTheSame(a: DeviceInfo, b: DeviceInfo) = a == b
        }
    }
}
