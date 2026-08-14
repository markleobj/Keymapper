package com.keymapper.app.mapping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.R
import com.keymapper.app.databinding.ItemMappingBinding
import com.keymapper.app.model.MappingConfig

class MappingAdapter(
    private val onToggle: (MappingConfig, Boolean) -> Unit,
    private val onDelete: (MappingConfig) -> Unit,
    private val onEdit: (MappingConfig) -> Unit
) : ListAdapter<MappingConfig, MappingAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMappingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemMappingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(config: MappingConfig) {
            binding.tvButton.text = config.button
            binding.tvAction.text = "${config.actionType.name} → (${"%.2f".format(config.targetX)}, ${"%.2f".format(config.targetY)})"
            binding.switchEnabled.isChecked = config.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(config, checked)
            }
            binding.btnDelete.setOnClickListener { onDelete(config) }
            binding.root.setOnClickListener { onEdit(config) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MappingConfig>() {
            override fun areItemsTheSame(a: MappingConfig, b: MappingConfig) = a.id == b.id
            override fun areContentsTheSame(a: MappingConfig, b: MappingConfig) = a == b
        }
    }
}
