package com.jms.makingsubtitle

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.MobileAds
import com.jms.makingsubtitle.data.datastore.ShowOptions
import com.jms.makingsubtitle.data.room.SubtitleFileDatabase
import com.jms.makingsubtitle.databinding.ActivityMainBinding
import com.jms.makingsubtitle.repository.MainRepository
import com.jms.makingsubtitle.ui.view.tutorial.InstructionFragment
import com.jms.makingsubtitle.ui.view.tutorial.TutorialActivity
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.ui.viewmodel.MainViewModelFactory
import com.jms.makingsubtitle.util.Contants.DATASTORE_NAME
import com.jms.makingsubtitle.util.ThemeHelper
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var navController: NavController



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

        lifecycleScope.launch {
            when(viewModel.getShowOptions()) {
                ShowOptions.SHOW_AGAIN.value -> {
                    val intent = Intent(this@MainActivity, TutorialActivity::class.java)
                    startActivity(intent)
                }
                else-> return@launch
            }
        }


        MobileAds.initialize(this) {}


    }
}