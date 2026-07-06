package io.github.amirhosseinkhosrobeigi.notes.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.databinding.ActivitySettingsBinding
import io.github.amirhosseinkhosrobeigi.notes.utils.ThemeHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeSettings()
        setupBottomNavigation()
    }

    private fun setupThemeSettings() {

        when (ThemeHelper.getCurrentTheme(this)) {
            ThemeHelper.THEME_SYSTEM -> binding.chipSystem.isChecked = true
            ThemeHelper.THEME_LIGHT -> binding.chipLight.isChecked = true
            ThemeHelper.THEME_DARK -> binding.chipDark.isChecked = true
        }

        binding.themeModeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->

            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val selectedTheme = when (checkedIds.first()) {
                R.id.chipSystem -> ThemeHelper.THEME_SYSTEM
                R.id.chipLight -> ThemeHelper.THEME_LIGHT
                R.id.chipDark -> ThemeHelper.THEME_DARK
                else -> return@setOnCheckedStateChangeListener
            }

            if (selectedTheme != ThemeHelper.getCurrentTheme(this)) {
                ThemeHelper.saveTheme(this, selectedTheme)
                showThemeChangedMessage()
            }
        }
    }

    private fun showThemeChangedMessage() {
        Toast.makeText(
            this,
            getString(R.string.theme_restart_required),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupBottomNavigation() {

        binding.bottomNavigation.selectedItemId = R.id.nav_settings

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_recycle_bin -> {
                    startActivity(Intent(this, RecycleBinActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_settings -> true

                else -> false
            }
        }
    }
}
