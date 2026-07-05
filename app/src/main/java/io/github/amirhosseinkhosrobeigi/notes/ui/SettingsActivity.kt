package io.github.amirhosseinkhosrobeigi.notes.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.amirhosseinkhosrobeigi.notes.ui.MainActivity
import io.github.amirhosseinkhosrobeigi.notes.ui.RecycleBinActivity
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_recycle_bin -> {
                    val intent = Intent(this, RecycleBinActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    // Already in SettingsActivity
                    true
                }
                else -> false
            }
        }
    }
}
