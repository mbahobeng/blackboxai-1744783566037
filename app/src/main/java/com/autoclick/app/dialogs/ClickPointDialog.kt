package com.autoclick.app.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.autoclick.app.R
import com.autoclick.app.databinding.DialogClickPointBinding
import com.autoclick.app.models.ClickPoint
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class ClickPointDialog : DialogFragment() {
    private var _binding: DialogClickPointBinding? = null
    private val binding get() = _binding!!
    
    private var clickPoint: ClickPoint? = null
    var onSaveClickPoint: ((ClickPoint) -> Unit)? = null
    
    private var startTimeInMillis: Long? = null
    private var endTimeInMillis: Long? = null
    
    companion object {
        private const val ARG_CLICK_POINT = "click_point"
        
        fun newInstance(clickPoint: ClickPoint? = null): ClickPointDialog {
            return ClickPointDialog().apply {
                arguments = Bundle().apply {
                    clickPoint?.let { putParcelable(ARG_CLICK_POINT, it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clickPoint = arguments?.getParcelable(ARG_CLICK_POINT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogClickPointBinding.inflate(layoutInflater)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        loadClickPoint()
    }

    private fun setupViews() {
        binding.sliderSize.apply {
            valueFrom = 24f
            valueTo = 200f
            stepSize = 1f
            value = clickPoint?.size ?: 48f
        }

        binding.touchArea.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    updateTouchIndicator(event.x, event.y)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.editStartTime.setOnClickListener { showTimePickerDialog(true) }
        binding.editEndTime.setOnClickListener { showTimePickerDialog(false) }

        binding.buttonSave.setOnClickListener { saveClickPoint() }
        binding.buttonCancel.setOnClickListener { dismiss() }

        binding.sliderSize.addOnChangeListener { _, value, _ ->
            updateTouchIndicatorSize(value)
        }
    }

    private fun loadClickPoint() {
        clickPoint?.let { point ->
            binding.apply {
                editName.setText(point.name)
                editDelay.setText(point.delay.toString())
                sliderSize.value = point.size
                
                point.startTime?.let {
                    startTimeInMillis = it
                    editStartTime.setText(formatTime(it))
                }
                
                point.endTime?.let {
                    endTimeInMillis = it
                    editEndTime.setText(formatTime(it))
                }

                // Position the touch indicator
                updateTouchIndicator(point.x, point.y)
            }
        }
    }

    private fun showTimePickerDialog(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val timeMillis = if (isStartTime) startTimeInMillis else endTimeInMillis
        
        timeMillis?.let {
            calendar.timeInMillis = it
        }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                
                val time = calendar.timeInMillis
                if (isStartTime) {
                    startTimeInMillis = time
                    binding.editStartTime.setText(formatTime(time))
                } else {
                    endTimeInMillis = time
                    binding.editEndTime.setText(formatTime(time))
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateTouchIndicator(x: Float, y: Float) {
        binding.touchIndicator.apply {
            translationX = x - width / 2
            translationY = y - height / 2
        }
    }

    private fun updateTouchIndicatorSize(size: Float) {
        binding.touchIndicator.apply {
            layoutParams = FrameLayout.LayoutParams(size.toInt(), size.toInt())
        }
    }

    private fun saveClickPoint() {
        val name = binding.editName.text.toString()
        val delay = binding.editDelay.text.toString().toLongOrNull() ?: 1000L
        val size = binding.sliderSize.value
        
        val x = binding.touchIndicator.translationX + binding.touchIndicator.width / 2
        val y = binding.touchIndicator.translationY + binding.touchIndicator.height / 2

        val newClickPoint = clickPoint?.copy(
            name = name,
            delay = delay,
            size = size,
            x = x,
            y = y,
            startTime = startTimeInMillis,
            endTime = endTimeInMillis
        ) ?: ClickPoint(
            name = name,
            delay = delay,
            size = size,
            x = x,
            y = y,
            startTime = startTimeInMillis,
            endTime = endTimeInMillis
        )

        if (validateClickPoint(newClickPoint)) {
            onSaveClickPoint?.invoke(newClickPoint)
            dismiss()
        }
    }

    private fun validateClickPoint(clickPoint: ClickPoint): Boolean {
        if (!clickPoint.isValidSize()) {
            showError(getString(R.string.error_invalid_size))
            return false
        }

        if (!clickPoint.isValidDelay()) {
            showError(getString(R.string.error_invalid_delay))
            return false
        }

        if (!clickPoint.isValidTime()) {
            showError(getString(R.string.error_invalid_time))
            return false
        }

        return true
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun formatTime(timeMillis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMillis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
