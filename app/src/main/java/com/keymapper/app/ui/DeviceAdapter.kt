package com.keymapper.app.ui

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.model.DeviceInfo

class DeviceAdapter(
    private val selectedAddressProvider: () -> String?,
    private val onSelect: (String) -> Unit,
    private val onUnselect: () -> Unit
) : ListAdapter<DeviceInfo, DeviceAdapter.VH>(DIFF) {

    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    fun updateConnectionState(state: ConnectionState) {
        connectionState = state
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val context = parent.context
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(context, 12), dp(context, 12), dp(context, 12), dp(context, 12))
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
            textSize = 11f
            setTextColor(Color.parseColor("#FF9E9E9E"))
        }
        val tvStatus = TextView(context).apply {
            textSize = 12f
            setPadding(0, dp(context, 2), 0, 0)
        }
        nameCol.addView(tvName)
        nameCol.addView(tvAddress)
        nameCol.addView(tvStatus)
        root.addView(nameCol)

        val btn = AppCompatButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER_VERTICAL }
        }
        root.addView(btn)

        return VH(root, tvName, tvAddress, tvStatus, btn)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        itemView: View,
        private val tvName: TextView,
        private val tvAddress: TextView,
        private val tvStatus: TextView,
        private val btn: AppCompatButton
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DeviceInfo) {
            tvName.text = item.name ?: "未知设备"
            tvAddress.text = item.address

            val selected = selectedAddressProvider() == item.address

            if (selected) {
                tvStatus.text = "✅ 已选中"
                tvStatus.setTextColor(Color.parseColor("#FF4CAF50"))
                btn.text = "取消选中"
                btn.setOnClickListener { onUnselect() }
            } else {
                tvStatus.text = "未选中"
                tvStatus.setTextColor(Color.parseColor("#FF9E9E9E"))
                btn.text = "选中"
                btn.setOnClickListener { onSelect(item.address) }
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
