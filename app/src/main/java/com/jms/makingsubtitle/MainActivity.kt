package com.jms.makingsubtitle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.jms.makingsubtitle.data.room.SubtitleFileDatabase
import com.jms.makingsubtitle.databinding.ActivityMainBinding
import com.jms.makingsubtitle.repository.MainRepository
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.ui.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var navController: NavController

    val viewModel: MainViewModel by lazy {
        val database = SubtitleFileDatabase.getInstance(this)
        val mainRepository = MainRepository(database)
        val factory = MainViewModelFactory(mainRepository, application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val host = supportFragmentManager.findFragmentById(R.id.host_fragment) as NavHostFragment
        navController = host.navController

    }
}