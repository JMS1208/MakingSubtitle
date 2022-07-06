package com.jms.makingsubtitle

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.jms.makingsubtitle.data.room.SubtitleFileDatabase
import com.jms.makingsubtitle.databinding.ActivityMainBinding
import com.jms.makingsubtitle.repository.MainRepository
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.ui.viewmodel.MainViewModelFactory
import com.jms.makingsubtitle.util.Contants.DATASTORE_NAME
import com.jms.makingsubtitle.util.ThemeHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var navController: NavController

    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

    val viewModel: MainViewModel by lazy {
        val database = SubtitleFileDatabase.getInstance(this)
        val mainRepository = MainRepository(database, dataStore)
        val factory = MainViewModelFactory(mainRepository, application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val host = supportFragmentManager.findFragmentById(R.id.host_fragment) as NavHostFragment
        navController = host.navController

        lifecycleScope.launch {
            val themePref = viewModel.getThemeMode()
            ThemeHelper.applyTheme(themePref)
        }

    }
}