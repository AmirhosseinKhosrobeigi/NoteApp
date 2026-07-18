package io.github.amirhosseinkhosrobeigi.notes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.adapter.recycler.NoteAdapter
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import io.github.amirhosseinkhosrobeigi.notes.databinding.FragmentNotesListBinding
import io.github.amirhosseinkhosrobeigi.notes.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.*

class NotesListFragment : Fragment() {

    private lateinit var binding: FragmentNotesListBinding
    private lateinit var db: DBHandler
    private lateinit var adapter: NoteAdapter

    private var noteList = ArrayList<NoteEntity>()
    private var allNotes = ArrayList<NoteEntity>()

    // Sort preferences
    private enum class SortType { NAME, DATE_NEWEST, DATE_OLDEST }

    private var currentSortType: SortType = SortType.NAME

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHandler.getDatabase(requireContext())

        setupRecycler()
        setupToolbar()
        setupFab()
        observeNotes()
    }

    // ---------------- Recycler ----------------
    private fun setupRecycler() {

        adapter = NoteAdapter(
            noteList,
            onDeleteClick = { note, _ ->
                showDeleteConfirmation(note)
            },

            onItemClick = { note ->
                val fragment = AddEditNoteFragment.newInstance(note.id, false)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteClick = { note ->
                toggleFavorite(note)
            }
        )

        binding.recyclerNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotes.adapter = adapter

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
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.noteDao().updateNoteState(note.id, false)
                }
            }
        }
        snackbar.setActionTextColor(requireContext().getColor(R.color.colorAccent))
        snackbar.show()

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
                snackbar.setActionTextColor(requireContext().getColor(R.color.colorAccent))
                snackbar.show()
            }
        }
    }

    private fun toggleFavorite(note: NoteEntity) {
        lifecycleScope.launch {
            val newFavoriteState = !note.isFavorite
            withContext(Dispatchers.IO) {
                db.noteDao().updateFavoriteState(note.id, newFavoriteState)
            }
            allNotes.find { it.id == note.id }?.let { existingNote ->
                val index = allNotes.indexOf(existingNote)
                if (index != -1) {
                    allNotes[index] = existingNote.copy(isFavorite = newFavoriteState)
                }
            }
            sortAndFilterNotes()
        }
    }

    private fun sortAndFilterNotes() {
        noteList.clear()

        when (currentSortType) {
            SortType.NAME -> {
                val faCollator = Collator.getInstance(Locale("fa"))
                val enCollator = Collator.getInstance(Locale.ENGLISH)

                noteList.addAll(allNotes.sortedWith { a, b ->
                    if (a.isFavorite != b.isFavorite) {
                        return@sortedWith if (a.isFavorite) -1 else 1
                    }
                    val aFirst = a.title.firstOrNull()
                    val bFirst = b.title.firstOrNull()

                    val aType = when {
                        aFirst?.isDigit() == true -> 0
                        aFirst?.code in 0x0600..0x06FF -> 1
                        else -> 2
                    }

                    val bType = when {
                        bFirst?.isDigit() == true -> 0
                        bFirst?.code in 0x0600..0x06FF -> 1
                        else -> 2
                    }

                    when {
                        aType != bType -> aType.compareTo(bType)
                        aType == 1 -> faCollator.compare(a.title, b.title)
                        else -> enCollator.compare(a.title, b.title)
                    }
                })
            }
            SortType.DATE_NEWEST -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd | HH:mm", Locale.ENGLISH)
                noteList.addAll(allNotes.sortedWith { a, b ->
                    if (a.isFavorite != b.isFavorite) {
                        return@sortedWith if (a.isFavorite) -1 else 1
                    }
                    val aDate = try {
                        dateFormat.parse(a.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    val bDate = try {
                        dateFormat.parse(b.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    bDate.compareTo(aDate)
                })
            }
            SortType.DATE_OLDEST -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd | HH:mm", Locale.ENGLISH)
                noteList.addAll(allNotes.sortedWith { a, b ->
                    if (a.isFavorite != b.isFavorite) {
                        return@sortedWith if (a.isFavorite) -1 else 1
                    }
                    val aDate = try {
                        dateFormat.parse(a.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    val bDate = try {
                        dateFormat.parse(b.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    aDate.compareTo(bDate)
                })
            }
        }

        adapter.notifyDataSetChanged()
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
                    showSortDialog()
                    true
                }

                else -> false
            }
        }
    }

    // ---------------- FAB ----------------
    private fun setupFab() {
        val mainActivity = activity as? MainActivity
        mainActivity?.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.img_add_note
        )?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val fragment = AddEditNoteFragment.newInstance(0, true)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = activity as? MainActivity
        mainActivity?.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.img_add_note
        )?.apply {
            visibility = View.GONE
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

                    // Apply sorting to newly loaded notes
                    sortAndFilterNotes()
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

        when (currentSortType) {
            SortType.NAME -> {
                val faCollator = Collator.getInstance(Locale("fa"))
                val enCollator = Collator.getInstance(Locale.ENGLISH)

                noteList.sortWith { a, b ->
                    if (a.isFavorite != b.isFavorite) {
                        return@sortWith if (a.isFavorite) -1 else 1
                    }
                    val aPersian = a.title.firstOrNull()?.code in 0x0600..0x06FF
                    val bPersian = b.title.firstOrNull()?.code in 0x0600..0x06FF

                    when {
                        aPersian && !bPersian -> -1
                        !aPersian && bPersian -> 1
                        aPersian -> faCollator.compare(a.title, b.title)
                        else -> enCollator.compare(a.title, b.title)
                    }
                }
            }
            SortType.DATE_NEWEST -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd | HH:mm", Locale.ENGLISH)
                noteList.sortWith { a, b ->
                    if (a.isFavorite != b.isFavorite) {
                        return@sortWith if (a.isFavorite) -1 else 1
                    }
                    val aDate = try {
                        dateFormat.parse(a.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    val bDate = try {
                        dateFormat.parse(b.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    bDate.compareTo(aDate)
                }
            }
            SortType.DATE_OLDEST -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd | HH:mm", Locale.ENGLISH)
                noteList.sortWith { a, b ->
                    if (a.isFavorite != b.isFavorite) {
                        return@sortWith if (a.isFavorite) -1 else 1
                    }
                    val aDate = try {
                        dateFormat.parse(a.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    val bDate = try {
                        dateFormat.parse(b.date)
                    } catch (e: Exception) {
                        Date(0)
                    }
                    aDate.compareTo(bDate)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    // ---------------- Sort Filter ----------------
    private fun sortNotes() {
        sortAndFilterNotes()
    }

    private fun showSortDialog() {
        val items =
            arrayOf("Sort by Name", "Sort by Date (Newest First)", "Sort by Date (Oldest First)")
        val checkedItem = when (currentSortType) {
            SortType.NAME -> 0
            SortType.DATE_NEWEST -> 1
            SortType.DATE_OLDEST -> 2
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort Notes")
            .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                currentSortType = when (which) {
                    0 -> SortType.NAME
                    1 -> SortType.DATE_NEWEST
                    2 -> SortType.DATE_OLDEST
                    else -> SortType.NAME
                }
                sortNotes()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
