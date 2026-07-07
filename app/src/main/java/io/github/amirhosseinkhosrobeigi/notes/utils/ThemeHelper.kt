package io.github.amirhosseinkhosrobeigi.notes.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    private const val PREF_NAME = "ThemePrefs"
    private const val PREF_THEME = "app_theme"

    fun initializeTheme(context: Context) {
        applyTheme(getCurrentTheme(context))
    }

    fun applyTheme(theme: Int) {
        when (theme) {
            THEME_SYSTEM ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            THEME_LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            THEME_DARK ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun saveTheme(context: Context, theme: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(PREF_THEME, theme)
            .apply()

        applyTheme(theme)
    }

    fun getCurrentTheme(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(PREF_THEME, THEME_SYSTEM)
    }
    
}
