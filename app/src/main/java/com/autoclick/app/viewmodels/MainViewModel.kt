package com.autoclick.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclick.app.database.AppDatabase
import com.autoclick.app.models.ClickConfiguration
import com.autoclick.app.models.ClickPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val clickPointDao = database.clickPointDao()
    private val configurationDao = database.clickConfigurationDao()

    private val _activeConfigurationId = MutableStateFlow<Long?>(null)
    
    val clickPoints: StateFlow<List<ClickPoint>> = _activeConfigurationId
        .flatMapLatest { configId ->
            if (configId != null) {
                clickPointDao.getClickPointsForConfiguration(configId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeConfiguration: StateFlow<ClickConfiguration?> = _activeConfigurationId
        .flatMapLatest { configId ->
            if (configId != null) {
                flow { emit(configurationDao.getConfigurationById(configId)) }
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val configurations: StateFlow<List<ClickConfiguration>> = configurationDao
        .getAllConfigurations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setActiveConfiguration(configId: Long?) {
        _activeConfigurationId.value = configId
    }

    fun addClickPoint(clickPoint: ClickPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val configId = _activeConfigurationId.value ?: return@launch
            val maxOrder = clickPointDao.getMaxOrderForConfiguration(configId) ?: -1
            val newPoint = clickPoint.copy(
                configurationId = configId,
                order = maxOrder + 1
            )
            clickPointDao.insert(newPoint)
        }
    }

    fun updateClickPoint(clickPoint: ClickPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            clickPointDao.update(clickPoint)
        }
    }

    fun deleteClickPoint(clickPoint: ClickPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            clickPointDao.delete(clickPoint)
            // Update order of remaining points
            clickPoint.configurationId?.let { configId ->
                clickPointDao.shiftOrdersDown(configId, clickPoint.order)
            }
        }
    }

    fun createConfiguration(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = ClickConfiguration(name = name)
            val configId = configurationDao.insert(config)
            setActiveConfiguration(configId)
        }
    }

    fun deleteConfiguration(configuration: ClickConfiguration) {
        viewModelScope.launch(Dispatchers.IO) {
            configurationDao.delete(configuration)
            if (_activeConfigurationId.value == configuration.id) {
                setActiveConfiguration(null)
            }
        }
    }

    fun duplicateConfiguration(configId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val newConfigId = configurationDao.duplicateConfiguration(configId)
            if (newConfigId != null) {
                // Duplicate all click points
                clickPointDao.getClickPointsForConfiguration(configId).first().forEach { point ->
                    val newPoint = point.copy(
                        id = 0,
                        configurationId = newConfigId
                    )
                    clickPointDao.insert(newPoint)
                }
            }
        }
    }

    fun updateConfigurationName(configId: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            configurationDao.getConfigurationById(configId)?.let { config ->
                configurationDao.update(config.copy(name = newName))
            }
        }
    }

    fun toggleConfigurationActive(configId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            configurationDao.toggleConfiguration(configId)
        }
    }

    fun reorderClickPoint(clickPoint: ClickPoint, newOrder: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            clickPointDao.reorderPoint(clickPoint, newOrder)
        }
    }

    fun importConfiguration(config: ClickConfiguration, clickPoints: List<ClickPoint>) {
        viewModelScope.launch(Dispatchers.IO) {
            val configId = configurationDao.insert(config.copy(id = 0))
            clickPoints.forEach { point ->
                val newPoint = point.copy(
                    id = 0,
                    configurationId = configId
                )
                clickPointDao.insert(newPoint)
            }
            setActiveConfiguration(configId)
        }
    }

    suspend fun getConfigurationById(configId: Long): ClickConfiguration? {
        return configurationDao.getConfigurationById(configId)
    }

    suspend fun getClickPointsForConfiguration(configId: Long): List<ClickPoint> {
        return clickPointDao.getClickPointsForConfiguration(configId).first()
    }

    fun importConfiguration(config: ClickConfiguration, clickPoints: List<ClickPoint>) {
        viewModelScope.launch(Dispatchers.IO) {
            val configId = configurationDao.insert(config)
            clickPoints.forEach { point ->
                val newPoint = point.copy(
                    id = 0,
                    configurationId = configId
                )
                clickPointDao.insert(newPoint)
            }
            setActiveConfiguration(configId)
        }
    }
}
