package ghasemi.abbas.note.utils

const val SETTINGS_PREF_KEY = "settings"
const val THEME_MODE_KEY = "themeMode"
const val STYLE_MODE_KEY = "styleMode"

enum class SortBy(val colName: String) {
    ID("id"),
    TITLE("title"),
    LAST_UPDATED_AT("last_updated_at"),
    CREATED_AT("created_at")
}