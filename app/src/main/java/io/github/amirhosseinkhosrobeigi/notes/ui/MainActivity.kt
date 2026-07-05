package io.github.amirhosseinkhosrobeigi.notes.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.adapter.recycler.NoteAdapter
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import io.github.amirhosseinkhosrobeigi.notes.databinding.ActivityMainBinding
import io.github.amirhosseinkhosrobeigi.notes.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DBHandler
    private lateinit var adapter: NoteAdapter

    private var noteList = ArrayList<NoteEntity>()
    private var allNotes = ArrayList<NoteEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHandler.getDatabase(this)

        setupRecycler()
        setupToolbar()
        setupFab()
        setupBottomNavigation()
        observeNotes()
    }

    // ---------------- Bottom Navigation ----------------
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in MainActivity (Home)
                    true
                }
                R.id.nav_recycle_bin -> {
                    val intent = Intent(this, RecycleBinActivity::class.java)
                    startActivity(intent)
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

    // ---------------- Recycler ----------------
    private fun setupRecycler() {

        adapter = NoteAdapter(
            noteList,
            onDeleteClick = { note, _ ->
                showDeleteConfirmation(note)
            },

            onItemClick = { note ->
                val intent = Intent(this, AddNotesActivity::class.java)
                intent.putExtra("notesId", note.id)
                intent.putExtra("newNotes", false)
                startActivity(intent)
            }
        )

        binding.recyclerNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotes.adapter = adapter

        // Setup swipe to delete
        adapter.attachSwipeToDelete(
            recyclerView = binding.recyclerNotes,
            onSwipeDelete = { note, _ ->
                showSwipeDeleteSnackbar(note)
            }
        )
    }

    private fun showSwipeDeleteSnackbar(note: NoteEntity) {
        val snackbar = Snackbar.make(
            binding.root,
            "یادداشت به سطل زباله منتقل شد",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("بازگردانی") {
            // Undo action - restore the note
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.noteDao().updateNoteState(note.id, false)
                }
            }
        }
        snackbar.setActionTextColor(getColor(R.color.colorAccent))
        snackbar.show()

        // Actually delete the note
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.noteDao().updateNoteState(note.id, true)
            }
        }
    }

    private fun showDeleteConfirmation(note: NoteEntity) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                db.noteDao().updateNoteState(note.id, true)
            }

            if (result > 0) {
                val snackbar = Snackbar.make(
                    binding.root,
                    "یادداشت به سطل زباله منتقل شد",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction("بازگردانی") {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            db.noteDao().updateNoteState(note.id, false)
                        }
                    }
                }
                snackbar.setActionTextColor(getColor(R.color.colorAccent))
                snackbar.show()
            }
        }
    }

    // ---------------- Toolbar + Search ----------------
    private fun setupToolbar() {

        binding.toolbar.inflateMenu(R.menu.main_menu)

        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)

        val searchView =
            searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = getString(R.string.search_hint)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterNotes(newText.orEmpty())
                return true
            }
        })

        binding.toolbar.setOnMenuItemClickListener { item ->

            when (item.itemId) {

                R.id.action_sort -> {
                    sortNotes()
                    true
                }

                else -> false
            }
        }
    }

    // ---------------- FAB ----------------
    private fun setupFab() {
        binding.imgAddNote.setOnClickListener {
            val intent = Intent(this, AddNotesActivity::class.java)
            intent.putExtra("newNotes", true)
            startActivity(intent)
        }
    }

    // ---------------- Load Notes ----------------
    private fun observeNotes() {

        lifecycleScope.launch {
            db.noteDao().getNotesByState(false)
                .collect { notes ->

                    allNotes.clear()
                    allNotes.addAll(notes)

                    noteList.clear()
                    noteList.addAll(notes)

                    adapter.notifyDataSetChanged()
                }
        }
    }

    // ---------------- Search Filter ----------------
    private fun filterNotes(query: String) {

        val filtered = if (query.isBlank()) {
            allNotes
        } else {
            allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
        }

        noteList.clear()
        noteList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    // ---------------- Sort Filter ----------------
    private fun sortNotes() {
        val faCollator = Collator.getInstance(Locale("fa"))
        val enCollator = Collator.getInstance(Locale.ENGLISH)

        val sortedNotes = allNotes.sortedWith { a, b ->

            val aPersian = a.title.firstOrNull()?.code in 0x0600..0x06FF
            val bPersian = b.title.firstOrNull()?.code in 0x0600..0x06FF

            when {
                aPersian && !bPersian -> -1
                !aPersian && bPersian -> 1
                aPersian -> faCollator.compare(a.title, b.title)
                else -> enCollator.compare(a.title, b.title)
            }
        }

        noteList.clear()
        noteList.addAll(sortedNotes)

        adapter.notifyDataSetChanged()
    }
}
