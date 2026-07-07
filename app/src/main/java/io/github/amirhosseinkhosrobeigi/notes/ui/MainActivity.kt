package io.github.amirhosseinkhosrobeigi.notes.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.databinding.ActivityMainBinding
import io.github.amirhosseinkhosrobeigi.notes.ui.fragments.AddEditNoteFragment
import io.github.amirhosseinkhosrobeigi.notes.ui.fragments.NotesListFragment
import io.github.amirhosseinkhosrobeigi.notes.ui.fragments.RecycleBinFragment
import io.github.amirhosseinkhosrobeigi.notes.ui.fragments.SettingsFragment
import io.github.amirhosseinkhosrobeigi.notes.utils.ThemeHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeHelper.initializeTheme(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            navigateToFragment(R.id.nav_home)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            navigateToFragment(item.itemId)
            true
        }
    }

    private fun navigateToFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.nav_home -> NotesListFragment()
            R.id.nav_recycle_bin -> RecycleBinFragment()
            R.id.nav_settings -> SettingsFragment()
            else -> return
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }

    fun navigateToAddEditNote(noteId: Int, isNewNote: Boolean) {
        val fragment = AddEditNoteFragment.newInstance(noteId, isNewNote)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
    }
}
