package com.jms.makingsubtitle.util

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.jms.makingsubtitle.data.datastore.ThemeMode

object ThemeHelper {

    fun applyTheme(themePref: String) {
        when (themePref) {
            ThemeMode.LIGHT.value -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            ThemeMode.DARK.value -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
            }
        }
    }
}