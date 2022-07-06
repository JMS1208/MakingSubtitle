package com.jms.makingsubtitle.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jms.makingsubtitle.repository.MainRepository
import java.lang.IllegalArgumentException

class MainViewModelFactory(
    private val mainRepository: MainRepository,
    private val application: Application
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mainRepository, application) as T
        }
        throw IllegalArgumentException("ViewModel class Not Found")
    }


}