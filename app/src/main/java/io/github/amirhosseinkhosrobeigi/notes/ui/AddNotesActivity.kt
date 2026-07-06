package io.github.amirhosseinkhosrobeigi.notes.ui

import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import io.github.amirhosseinkhosrobeigi.notes.databinding.ActivityAddNotesBinding
import io.github.amirhosseinkhosrobeigi.notes.utils.PersianDate
import io.github.amirhosseinkhosrobeigi.notes.utils.ThemeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNotesBinding
    private lateinit var db: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeHelper.initializeTheme(this)

        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHandler.getDatabase(this)

        val isNewNote = intent.getBooleanExtra("newNotes", true)
        val noteId = intent.getIntExtra("notesId", 0)

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
                    finish()
                }
            }
        }

        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun getDate(): String {
        val persianDate = PersianDate()
        val currentDate = "${persianDate.year}/${persianDate.month}/${persianDate.day}"
        val currentTime = "${persianDate.hour}:${persianDate.min}"
        return "$currentDate | $currentTime"
    }

    private fun showText(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
