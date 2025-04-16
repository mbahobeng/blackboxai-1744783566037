package com.autoclick.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoclick.app.databinding.ItemConfigurationBinding
import com.autoclick.app.models.ClickConfiguration

class ConfigurationAdapter(
    private val onConfigurationSelected: (ClickConfiguration) -> Unit
) : ListAdapter<ClickConfiguration, ConfigurationAdapter.ViewHolder>(ConfigurationDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConfigurationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    inner class ViewHolder(
        private val binding: ItemConfigurationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val oldPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                    onConfigurationSelected(getItem(position))
                }
            }
        }

        fun bind(configuration: ClickConfiguration, isSelected: Boolean) {
            binding.apply {
                textName.text = configuration.name
                textPointsCount.text = itemView.context.getString(
                    R.string.configuration_points_count,
                    configuration.clickPoints.size
                )
                radioSelected.isChecked = isSelected
            }
        }
    }

    private class ConfigurationDiffCallback : DiffUtil.ItemCallback<ClickConfiguration>() {
        override fun areItemsTheSame(oldItem: ClickConfiguration, newItem: ClickConfiguration): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClickConfiguration, newItem: ClickConfiguration): Boolean {
            return oldItem == newItem
        }
    }
}
