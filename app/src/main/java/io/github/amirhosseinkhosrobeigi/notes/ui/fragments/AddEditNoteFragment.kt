package io.github.amirhosseinkhosrobeigi.notes.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import io.github.amirhosseinkhosrobeigi.notes.databinding.FragmentAddEditNoteBinding
import io.github.amirhosseinkhosrobeigi.notes.ui.MainActivity
import io.github.amirhosseinkhosrobeigi.notes.utils.PersianDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEditNoteFragment : Fragment() {

    private lateinit var binding: FragmentAddEditNoteBinding
    private lateinit var db: DBHandler

    companion object {
        private const val ARG_NOTE_ID = "noteId"
        private const val ARG_IS_NEW_NOTE = "isNewNote"

        fun newInstance(noteId: Int, isNewNote: Boolean): AddEditNoteFragment {
            val args = Bundle().apply {
                putInt(ARG_NOTE_ID, noteId)
                putBoolean(ARG_IS_NEW_NOTE, isNewNote)
            }
            return AddEditNoteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHandler.getDatabase(requireContext())

        val mainActivity = activity as? MainActivity
        mainActivity?.apply {
            findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.GONE
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.img_add_note)?.visibility = View.GONE
        }

        val isNewNote = arguments?.getBoolean(ARG_IS_NEW_NOTE, true) ?: true
        val noteId = arguments?.getInt(ARG_NOTE_ID, 0) ?: 0

        if (isNewNote) {
            binding.txtDate.text = getDate()
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val note = db.noteDao().getNoteById(noteId)
                if (note != null) {
                    withContext(Dispatchers.Main) {
                        val edit = Editable.Factory.getInstance()
                        binding.edtTitleNote.text = edit.newEditable(note.title)
                        binding.edtDetailNote.text = edit.newEditable(note.detail)
                        binding.txtDate.text = note.date.toString()
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.edtTitleNote.text.toString()
            val detail = binding.edtDetailNote.text.toString()

            if (title.isBlank()) {
                showText("عنوان نمیتواند خالی باشد")
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val date = getDate()
                val note = if (isNewNote) {
                    NoteEntity(
                        id = 0,
                        title = title,
                        detail = detail,
                        deleteState = false,
                        date = date
                    )
                } else {
                    val existingNote = db.noteDao().getNoteById(noteId)
                    existingNote?.copy(
                        title = title,
                        detail = detail,
                        date = date
                    ) ?: return@launch
                }

                if (isNewNote) {
                    db.noteDao().insertNote(note)
                } else {
                    db.noteDao().updateNote(note)
                }

                withContext(Dispatchers.Main) {
                    val message = if (isNewNote) "یادداشت ذخیره شد" else "یادداشت بروزرسانی شد"
                    showText(message)
                    parentFragmentManager.popBackStack()
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = activity as? MainActivity
        mainActivity?.apply {
            findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.VISIBLE
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.img_add_note)?.visibility = View.VISIBLE
        }
    }

    private fun getDate(): String {
        val persianDate = PersianDate()
        val currentDate = "${persianDate.year}/${persianDate.month}/${persianDate.day}"
        val currentTime = "${persianDate.hour}:${persianDate.min}"
        return "$currentDate | $currentTime"
    }

    private fun showText(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
