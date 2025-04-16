package com.autoclick.app.activities

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoclick.app.R
import com.autoclick.app.adapters.ClickPointAdapter
import com.autoclick.app.databinding.ActivityMainBinding
import com.autoclick.app.dialogs.ClickPointDialog
import com.autoclick.app.models.ClickPoint
import com.autoclick.app.services.AutoClickService
import com.autoclick.app.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var clickPointAdapter: ClickPointAdapter
    private var isServiceRunning = false

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
        private const val ACCESSIBILITY_SETTINGS_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        checkPermissions()
    }

    private fun setupRecyclerView() {
        clickPointAdapter = ClickPointAdapter(
            onEditClick = { clickPoint -> showEditDialog(clickPoint) },
            onDeleteClick = { clickPoint -> deleteClickPoint(clickPoint) }
        )
        
        binding.recyclerViewClickPoints.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = clickPointAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddClickPoint.setOnClickListener {
            showAddDialog()
        }

        binding.buttonStartService.setOnClickListener {
            toggleService()
        }

        binding.buttonSaveConfig.setOnClickListener {
            showSaveConfigDialog()
        }

        binding.buttonLoadConfig.setOnClickListener {
            showLoadConfigDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.clickPoints.collect { points ->
                clickPointAdapter.submitList(points)
                updateEmptyState(points.isEmpty())
            }
        }

        lifecycleScope.launch {
            viewModel.activeConfiguration.collect { config ->
                binding.textCurrentConfig.text = config?.name ?: getString(R.string.no_active_configuration)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showAddDialog() {
        ClickPointDialog.newInstance().apply {
            onSaveClickPoint = { clickPoint ->
                viewModel.addClickPoint(clickPoint)
            }
        }.show(supportFragmentManager, "AddClickPoint")
    }

    private fun showEditDialog(clickPoint: ClickPoint) {
        ClickPointDialog.newInstance(clickPoint).apply {
            onSaveClickPoint = { updatedPoint ->
                viewModel.updateClickPoint(updatedPoint)
            }
        }.show(supportFragmentManager, "EditClickPoint")
    }

    private fun deleteClickPoint(clickPoint: ClickPoint) {
        viewModel.deleteClickPoint(clickPoint)
        Snackbar.make(binding.root, R.string.point_deleted, Snackbar.LENGTH_SHORT).show()
    }

    private fun toggleService() {
        if (!checkPermissions()) return

        val serviceIntent = Intent(this, AutoClickService::class.java).apply {
            action = if (isServiceRunning) "STOP" else "START"
        }
        
        if (isServiceRunning) {
            stopService(serviceIntent)
        } else {
            startForegroundService(serviceIntent)
        }
        
        isServiceRunning = !isServiceRunning
        updateServiceButton()
    }

    private fun updateServiceButton() {
        binding.buttonStartService.setText(
            if (isServiceRunning) R.string.stop_service else R.string.start_service
        )
    }

    private fun checkPermissions(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return false
        }

        if (!isAccessibilityServiceEnabled()) {
            requestAccessibilityPermission()
            return false
        }

        return true
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, ACCESSIBILITY_SETTINGS_REQUEST_CODE)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return enabledServices.any { it.id.contains(packageName) }
    }

    private fun showSaveConfigDialog() {
        // Implementation for saving configuration
    }

    private fun showLoadConfigDialog() {
        // Implementation for loading configuration
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, R.string.error_overlay_permission, Toast.LENGTH_LONG).show()
                }
            }
            ACCESSIBILITY_SETTINGS_REQUEST_CODE -> {
                if (!isAccessibilityServiceEnabled()) {
                    Toast.makeText(this, R.string.error_accessibility_service, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
