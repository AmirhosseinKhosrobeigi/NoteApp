package io.github.amirhosseinkhosrobeigi.notes.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.adapter.recycler.RecycleBinAdapter
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.databinding.ActivityRecycleBinBinding
import io.github.amirhosseinkhosrobeigi.notes.ui.SettingsActivity
import kotlinx.coroutines.launch

class RecycleBinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecycleBinBinding
    private lateinit var adapter: RecycleBinAdapter
    private lateinit var db: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycleBinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHandler.getDatabase(this)

        adapter = RecycleBinAdapter(
            context = this,
            dao = db.noteDao(),
            notes = arrayListOf()
        )

        binding.recyclerNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotes.adapter = adapter

        setupBottomNavigation()

        lifecycleScope.launch {
            db.noteDao().getNotesByState(true)
                .collect { notes ->
                    adapter.updateData(notes)
                }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_recycle_bin

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_recycle_bin -> {
                    // Already in RecycleBinActivity
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
