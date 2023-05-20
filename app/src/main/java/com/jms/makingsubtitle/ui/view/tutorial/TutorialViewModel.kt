package com.jms.makingsubtitle.ui.view.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jms.makingsubtitle.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val repository: MainRepository
): ViewModel() {

    fun saveShowOptions(value: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveShowOptions(value)
    }
}