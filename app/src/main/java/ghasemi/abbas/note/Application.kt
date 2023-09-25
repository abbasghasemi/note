package ghasemi.abbas.note

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.HiltAndroidApp
import ghasemi.abbas.note.utils.SETTINGS_PREF_KEY
import ghasemi.abbas.note.utils.THEME_MODE_KEY
import ghasemi.abbas.note.utils.ThemeUtil

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs: SharedPreferences = getSharedPreferences(SETTINGS_PREF_KEY, Context.MODE_PRIVATE)
        val themePref = prefs.getString(THEME_MODE_KEY, ThemeUtil.SYSTEM)
        ThemeUtil.applyTheme(themePref!!)
    }
}