package com.jms.makingsubtitle.ui.view.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.jms.makingsubtitle.MainActivity
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.databinding.FragmentSettingsBinding
import com.jms.makingsubtitle.ui.viewmodel.MainViewModel
import com.jms.makingsubtitle.data.datastore.LeafTimeMode
import com.jms.makingsubtitle.util.ThemeHelper
import com.jms.makingsubtitle.data.datastore.ThemeMode
import com.jms.makingsubtitle.data.datastore.VibrationOptions
import com.jms.makingsubtitle.ui.view.tutorial.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!

    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbar.apply {
            setNavigationIcon(R.drawable.ic_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.btnShowInstructionDialog.setOnClickListener {
            val intent = Intent(requireContext(), TutorialActivity::class.java)
            startActivity(intent)
        }

        binding.btnLicense.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Open-Source")
                .setMessage(R.string.licenseContents)
                .setPositiveButton("닫기", null)
                .create()
                .show()

        }

        saveSettings()
        loadSettings()
    }

    private fun saveSettings() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkId ->
            val value = when (checkId) {
                R.id.rb_light -> ThemeMode.LIGHT.value
                R.id.rb_dark -> ThemeMode.DARK.value
                R.id.rb_default -> ThemeMode.DEFAULT.value
                else -> return@setOnCheckedChangeListener
            }
            viewModel.saveThemeMode(value)
            ThemeHelper.applyTheme(value)
        }
        binding.rgLeafTime.setOnCheckedChangeListener { _, checkId ->
            val value = when (checkId) {
                R.id.rb_leaf_time_1 -> LeafTimeMode.ONE_SECOND.value
                R.id.rb_leaf_time_3 -> LeafTimeMode.THREE_SECOND.value
                R.id.rb_leaf_time_5 -> LeafTimeMode.FIVE_SECOND.value
                R.id.rb_leaf_time_7 -> LeafTimeMode.SEVEN_SECOND.value
                R.id.rb_leaf_time_10 -> LeafTimeMode.TEN_SECOND.value
                else -> return@setOnCheckedChangeListener
            }
            viewModel.saveLeafTimeMode(value)
        }



        binding.swVibrationOptions.setOnCheckedChangeListener { _, isChecked ->
            val value = if (isChecked) {
                VibrationOptions.ACTIVATE.value
            } else {
                VibrationOptions.INACTIVATE.value
            }
            viewModel.saveVibrationOptions(value)

        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val buttonIdThemeMode = when (viewModel.getThemeMode()) {
                ThemeMode.LIGHT.value -> R.id.rb_light
                ThemeMode.DARK.value -> R.id.rb_dark
                ThemeMode.DEFAULT.value -> R.id.rb_default
                else -> return@launch
            }
            binding.rgTheme.check(buttonIdThemeMode)


        }
        lifecycleScope.launch {
            val buttonIdLeafTimeMode = when (viewModel.getLeafTimeMode()) {
                LeafTimeMode.ONE_SECOND.value -> R.id.rb_leaf_time_1
                LeafTimeMode.THREE_SECOND.value -> R.id.rb_leaf_time_3
                LeafTimeMode.FIVE_SECOND.value -> R.id.rb_leaf_time_5
                LeafTimeMode.SEVEN_SECOND.value -> R.id.rb_leaf_time_7
                LeafTimeMode.TEN_SECOND.value -> R.id.rb_leaf_time_10
                else -> return@launch
            }
            binding.rgLeafTime.check(buttonIdLeafTimeMode)
        }


        lifecycleScope.launch {
            binding.swVibrationOptions.isChecked = when (viewModel.getVibrationOptions()) {
                VibrationOptions.ACTIVATE.value -> true
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}