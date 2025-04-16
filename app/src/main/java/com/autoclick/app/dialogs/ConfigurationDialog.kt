package com.autoclick.app.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoclick.app.R
import com.autoclick.app.adapters.ConfigurationAdapter
import com.autoclick.app.databinding.DialogConfigurationBinding
import com.autoclick.app.models.ClickConfiguration
import com.autoclick.app.utils.ConfigurationManager
import com.autoclick.app.utils.showSnackbar
import com.autoclick.app.utils.showToast
import com.autoclick.app.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ConfigurationDialog : DialogFragment() {
    private var _binding: DialogConfigurationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    private lateinit var configManager: ConfigurationManager
    
    private var mode: DialogMode = DialogMode.SAVE
    private var currentConfig: ClickConfiguration? = null

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportConfiguration(uri)
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importConfiguration(uri)
            }
        }
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_CONFIG_ID = "config_id"

        fun newInstance(mode: DialogMode, configId: Long? = null): ConfigurationDialog {
            return ConfigurationDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MODE, mode)
                    configId?.let { putLong(ARG_CONFIG_ID, it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configManager = ConfigurationManager(requireContext())
        mode = arguments?.getSerializable(ARG_MODE) as DialogMode
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_AutoClick_MaterialDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadCurrentConfiguration()
    }

    private lateinit var configurationAdapter: ConfigurationAdapter

    private fun setupViews() {
        binding.apply {
            textTitle.text = when (mode) {
                DialogMode.SAVE -> getString(R.string.dialog_title_new_configuration)
                DialogMode.LOAD -> getString(R.string.dialog_title_edit_configuration)
            }

            buttonExport.setOnClickListener { initiateExport() }
            buttonImport.setOnClickListener { initiateImport() }
            buttonSave.setOnClickListener { handleSave() }
            buttonCancel.setOnClickListener { dismiss() }

            // Show/hide views based on mode
            layoutName.visibility = if (mode == DialogMode.SAVE) View.VISIBLE else View.GONE
            buttonExport.visibility = View.VISIBLE // Always show export
            buttonImport.visibility = View.VISIBLE // Always show import
            buttonSave.text = when (mode) {
                DialogMode.SAVE -> getString(R.string.action_save)
                DialogMode.LOAD -> getString(R.string.action_load)
            }

            // Setup configurations container visibility
            containerConfigurations.visibility = if (mode == DialogMode.LOAD) View.VISIBLE else View.GONE

            // Setup RecyclerView for configurations
            recyclerConfigurations.apply {
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                configurationAdapter = ConfigurationAdapter { config ->
                    currentConfig = config
                }
                adapter = configurationAdapter
            }

            // Observe configurations
            if (mode == DialogMode.LOAD) {
                lifecycleScope.launch {
                    viewModel.configurations.collect { configs ->
                        configurationAdapter.submitList(configs)
                        // Show/hide empty state
                        recyclerConfigurations.visibility = if (configs.isNotEmpty()) View.VISIBLE else View.GONE
                        textNoConfigurations.visibility = if (configs.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun handleSave() {
        when (mode) {
            DialogMode.SAVE -> {
                val name = binding.editName.text.toString().trim()
                if (name.isEmpty()) {
                    binding.layoutName.error = getString(R.string.error_empty_name)
                    return
                }

                lifecycleScope.launch {
                    // Check if a configuration with this name already exists
                    val existingConfig = viewModel.configurations.value.find { 
                        it.name == name && it.id != (currentConfig?.id ?: -1)
                    }
                    
                    if (existingConfig != null) {
                        binding.layoutName.error = getString(R.string.error_duplicate_name)
                        return@launch
                    }

                    if (currentConfig != null) {
                        viewModel.updateConfigurationName(currentConfig!!.id, name)
                        requireContext().showToast(getString(R.string.success_configuration_saved))
                    } else {
                        viewModel.createConfiguration(name)
                        requireContext().showToast(getString(R.string.success_configuration_saved))
                    }
                    dismiss()
                }
            }
            DialogMode.LOAD -> {
                if (currentConfig == null) {
                    view?.showSnackbar(getString(R.string.error_no_configuration_selected))
                    return
                }
                
                viewModel.setActiveConfiguration(currentConfig!!.id)
                requireContext().showToast(getString(R.string.success_configuration_loaded))
                dismiss()
            }
        }
    }

    private fun loadCurrentConfiguration() {
        arguments?.getLong(ARG_CONFIG_ID, -1L)?.let { configId ->
            if (configId != -1L) {
                lifecycleScope.launch {
                    currentConfig = viewModel.getConfigurationById(configId)
                    currentConfig?.let { config ->
                        binding.editName.setText(config.name)
                    }
                }
            }
        }
    }

    private fun initiateExport() {
        when (mode) {
            DialogMode.SAVE -> {
                val name = binding.editName.text.toString().trim()
                if (name.isEmpty()) {
                    binding.layoutName.error = getString(R.string.error_empty_name)
                    return
                }

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, configManager.generateExportFileName(name))
                }
                exportLauncher.launch(intent)
            }
            DialogMode.LOAD -> {
                currentConfig?.let { config ->
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                        putExtra(Intent.EXTRA_TITLE, configManager.generateExportFileName(config.name))
                    }
                    exportLauncher.launch(intent)
                } ?: run {
                    view?.showSnackbar(getString(R.string.error_no_configuration_selected))
                }
            }
        }
    }

    private fun initiateImport() {
        // Clear any previous selection when importing
        currentConfig = null
        
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }

    private fun exportConfiguration(uri: Uri) {
        lifecycleScope.launch {
            try {
                val config = currentConfig ?: return@launch
                val clickPoints = viewModel.getClickPointsForConfiguration(config.id)
                
                configManager.exportConfiguration(config, clickPoints, uri)
                    .onSuccess {
                        requireContext().showToast(getString(R.string.success_config_exported))
                        dismiss()
                    }
                    .onFailure { e ->
                        view?.showSnackbar(getString(R.string.error_export_failed))
                    }
            } catch (e: Exception) {
                view?.showSnackbar(getString(R.string.error_export_failed))
            }
        }
    }

    private fun importConfiguration(uri: Uri) {
        lifecycleScope.launch {
            try {
                configManager.importConfiguration(uri)
                    .onSuccess { (config, clickPoints) ->
                        if (configManager.validateConfiguration(config, clickPoints)) {
                            viewModel.importConfiguration(config, clickPoints)
                            requireContext().showToast(getString(R.string.success_config_imported))
                            // Refresh the list if in LOAD mode
                            if (mode == DialogMode.LOAD) {
                                configurationAdapter.submitList(viewModel.configurations.value)
                            }
                            dismiss()
                        } else {
                            view?.showSnackbar(getString(R.string.error_invalid_config))
                        }
                    }
                    .onFailure { e ->
                        view?.showSnackbar(getString(R.string.error_import_failed))
                    }
            } catch (e: Exception) {
                view?.showSnackbar(getString(R.string.error_import_failed))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class DialogMode {
        SAVE,
        LOAD
    }
}
