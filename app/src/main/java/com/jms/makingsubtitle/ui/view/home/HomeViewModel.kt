package com.jms.makingsubtitle.ui.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.repository.MainRepository
import com.jms.makingsubtitle.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepository
): ViewModel() {

    val allSubtitleList: LiveData<List<SubtitleFile>> = repository.getAllSubtitleFileList()

    fun insertSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertSubtitleFile(subtitleFile)
    }

    fun deleteSubtitleFileByUUID(uuid: UUID) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteSubtitleFileByUUID(uuid)
    }

    fun deleteAllSubtitleFiles() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllSubtitleFiles()
    }

    fun updateSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        subtitleFile.lastUpdate = Date()
        repository.updateSubtitleFile(subtitleFile)

    }

}