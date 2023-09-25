package ghasemi.abbas.note.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ghasemi.abbas.note.utils.SETTINGS_PREF_KEY
import ghasemi.abbas.note.utils.STYLE_MODE_KEY
import ghasemi.abbas.note.utils.SortBy
import javax.inject.Inject

class PrefsManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val SORT_BY = "sortBy"
        private const val FAVORITE_PINNED = "favoritePinned"
    }

    private val preferences = context.getSharedPreferences(SETTINGS_PREF_KEY, Context.MODE_PRIVATE)

    fun setSortBy(sortBy: String) {
        preferences.edit()
            .putString(SORT_BY, sortBy)
            .apply()
    }

    fun sortBy(): String? {
        return preferences.getString(SORT_BY, SortBy.ID.colName)
    }

    fun favoritePinned(pinned: Boolean) {
        preferences.edit()
            .putBoolean(FAVORITE_PINNED, pinned)
            .apply()
    }

    fun favoritePinnedStatus(): Boolean {
        return preferences.getBoolean(FAVORITE_PINNED, false)
    }

    fun getViewStyle(): String? {
        val prefs = context.getSharedPreferences(SETTINGS_PREF_KEY, Context.MODE_PRIVATE)
        return prefs.getString(STYLE_MODE_KEY, "list")
    }
}