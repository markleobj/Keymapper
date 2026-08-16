package com.keymapper.app.ui

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.model.DeviceInfo

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
        val context = parent.context
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, dp(context, 12), 0, dp(context, 12))
        }

        val nameCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvName = TextView(context).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#FF212121"))
        }
        val tvAddress = TextView(context).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#FF9E9E9E"))
            setPadding(0, dp(context, 2), 0, 0)
        }
        val tvStatus = TextView(context).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#FF4CAF50"))
            setPadding(0, dp(context, 2), 0, 0)
        }
        nameCol.addView(tvName)
        nameCol.addView(tvAddress)
        nameCol.addView(tvStatus)
        root.addView(nameCol)

        val btnAction = Button(context).apply {
            text = "连接"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        }
        root.addView(btnAction)

        return VH(root, tvName, tvAddress, tvStatus, btnAction)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class VH(
        itemView: View,
        private val tvName: TextView,
        private val tvAddress: TextView,
        private val tvStatus: TextView,
        private val btnAction: Button
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DeviceInfo) {
            tvName.text = item.name ?: "未知设备"
            tvAddress.text = item.address

            val isConnected = connectedAddress == item.address
            when {
                connectionState == ConnectionState.CONNECTING && isConnected -> {
                    tvStatus.text = "连接中…"
                    btnAction.text = "取消"
                    btnAction.setOnClickListener { onDisconnect() }
                }
                isConnected && connectionState == ConnectionState.CONNECTED -> {
                    tvStatus.text = "已连接"
                    btnAction.text = "断开"
                    btnAction.setOnClickListener { onDisconnect() }
                }
                else -> {
                    tvStatus.text = ""
                    btnAction.text = "连接"
                    btnAction.setOnClickListener { onConnect(item.address) }
                }
            }
        }
    }

    companion object {
        private fun dp(context: android.content.Context, value: Int): Int =
            (value * context.resources.displayMetrics.density).toInt()

        private val DIFF = object : DiffUtil.ItemCallback<DeviceInfo>() {
            override fun areItemsTheSame(a: DeviceInfo, b: DeviceInfo) = a.address == b.address
            override fun areContentsTheSame(a: DeviceInfo, b: DeviceInfo) = a == b
        }
    }
}
