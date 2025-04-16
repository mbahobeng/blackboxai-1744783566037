package com.autoclick.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoclick.app.databinding.ItemClickPointBinding
import com.autoclick.app.models.ClickPoint
import java.text.SimpleDateFormat
import java.util.*

class ClickPointAdapter(
    private val onEditClick: (ClickPoint) -> Unit,
    private val onDeleteClick: (ClickPoint) -> Unit,
    private val onMoveUp: ((ClickPoint) -> Unit)? = null,
    private val onMoveDown: ((ClickPoint) -> Unit)? = null
) : ListAdapter<ClickPoint, ClickPointAdapter.ClickPointViewHolder>(ClickPointDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClickPointViewHolder {
        val binding = ItemClickPointBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClickPointViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClickPointViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ClickPointViewHolder(
        private val binding: ItemClickPointBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickPoint: ClickPoint, position: Int) {
            with(binding) {
                // Set basic info
                textPointName.text = clickPoint.name.ifEmpty { "Point ${position + 1}" }
                textCoordinates.text = "X: ${clickPoint.x}, Y: ${clickPoint.y}"
                textSize.text = "Size: ${clickPoint.size}dp"
                textDelay.text = "Delay: ${clickPoint.delay}ms"

                // Set time info if available
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                textTimes.text = when {
                    clickPoint.startTime != null && clickPoint.endTime != null -> {
                        "Time: ${timeFormat.format(Date(clickPoint.startTime))} - ${
                            timeFormat.format(Date(clickPoint.endTime))
                        }"
                    }
                    clickPoint.startTime != null -> {
                        "Start: ${timeFormat.format(Date(clickPoint.startTime))}"
                    }
                    clickPoint.endTime != null -> {
                        "End: ${timeFormat.format(Date(clickPoint.endTime))}"
                    }
                    else -> "No time set"
                }

                // Setup buttons
                buttonEdit.setOnClickListener { onEditClick(clickPoint) }
                buttonDelete.setOnClickListener { onDeleteClick(clickPoint) }

                // Setup move buttons if callbacks are provided
                buttonMoveUp.apply {
                    setOnClickListener { onMoveUp?.invoke(clickPoint) }
                    isEnabled = position > 0 && onMoveUp != null
                    alpha = if (isEnabled) 1f else 0.5f
                }

                buttonMoveDown.apply {
                    setOnClickListener { onMoveDown?.invoke(clickPoint) }
                    isEnabled = position < itemCount - 1 && onMoveDown != null
                    alpha = if (isEnabled) 1f else 0.5f
                }
            }
        }
    }

    private class ClickPointDiffCallback : DiffUtil.ItemCallback<ClickPoint>() {
        override fun areItemsTheSame(oldItem: ClickPoint, newItem: ClickPoint): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClickPoint, newItem: ClickPoint): Boolean {
            return oldItem == newItem
        }
    }
}
