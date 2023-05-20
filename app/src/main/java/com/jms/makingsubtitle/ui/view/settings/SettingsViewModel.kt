package com.jms.makingsubtitle.ui.view.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jms.makingsubtitle.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: MainRepository
): ViewModel() {
    //DataStore
    fun saveThemeMode(value: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveThemeMode(value)
    }
    fun saveLeafTimeMode(value: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveLeafTimeMode(value)
    }
    fun saveVibrationOptions(value: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveVibrationOptions(value)
    }
    suspend fun getThemeMode() = withContext(Dispatchers.IO) {
        repository.getThemeMode().first()
    }
    suspend fun getLeafTimeMode() = withContext(Dispatchers.IO) {
        repository.getLeafTimeMode().first()
    }
    suspend fun getVibrationOptions() = withContext(Dispatchers.IO) {
        repository.getVibrationOptions().first()
    }
}