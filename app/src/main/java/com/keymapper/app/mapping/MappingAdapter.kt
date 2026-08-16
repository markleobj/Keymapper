package com.keymapper.app.mapping

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.model.MappingConfig

class MappingAdapter(
    private val onToggle: (MappingConfig, Boolean) -> Unit,
    private val onDelete: (MappingConfig) -> Unit,
    private val onEdit: (MappingConfig) -> Unit
) : ListAdapter<MappingConfig, MappingAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val context = parent.context
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(context, 12), dp(context, 12), dp(context, 12), dp(context, 12))
            isClickable = true
            isFocusable = true
        }

        val textCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvButton = TextView(context).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val tvAction = TextView(context).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#FF757575"))
            setPadding(0, dp(context, 2), 0, 0)
        }
        textCol.addView(tvButton)
        textCol.addView(tvAction)
        root.addView(textCol)

        val switchEnabled = SwitchCompat(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        }
        root.addView(switchEnabled)

        val btnDelete = Button(context).apply {
            text = "🗑"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        }
        root.addView(btnDelete)

        return VH(root, tvButton, tvAction, switchEnabled, btnDelete)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        itemView: View,
        private val tvButton: TextView,
        private val tvAction: TextView,
        private val switchEnabled: SwitchCompat,
        private val btnDelete: Button
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(config: MappingConfig) {
            tvButton.text = config.button
            tvAction.text = "${config.actionType.name} → (${"%.2f".format(config.targetX)}, ${"%.2f".format(config.targetY)})"

            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = config.enabled
            switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(config, checked)
            }

            btnDelete.setOnClickListener { onDelete(config) }
            itemView.setOnClickListener { onEdit(config) }
        }
    }

    companion object {
        private fun dp(context: android.content.Context, value: Int): Int =
            (value * context.resources.displayMetrics.density).toInt()

        private val DIFF = object : DiffUtil.ItemCallback<MappingConfig>() {
            override fun areItemsTheSame(a: MappingConfig, b: MappingConfig) = a.id == b.id
            override fun areContentsTheSame(a: MappingConfig, b: MappingConfig) = a == b
        }
    }
}
