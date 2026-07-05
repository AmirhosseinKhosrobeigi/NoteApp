package io.github.amirhosseinkhosrobeigi.notes.adapter.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.data.local.dao.NoteDao
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import io.github.amirhosseinkhosrobeigi.notes.databinding.ListItemRecycleBinBinding
import io.github.amirhosseinkhosrobeigi.notes.utils.showCustomAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecycleBinAdapter(
    private val context: Context,
    private val dao: NoteDao,
    private var notes: ArrayList<NoteEntity>
) : RecyclerView.Adapter<RecycleBinAdapter.RecycleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder =
        RecycleViewHolder(
            ListItemRecycleBinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class RecycleViewHolder(
        private val binding: ListItemRecycleBinBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteEntity) {
            binding.txtTitleNotes.text = note.title

            binding.imgDeleteNotes.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                context.showCustomAlert(
                    title = "حذف دائمی یادداشت",
                    message = "آیا میخواهید یادداشت برای همیشه حذف شود؟",
                    iconResId = R.drawable.ic_delete,
                    positiveText = "بله",
                    negativeText = "خیر",
                    positiveAction = {
                        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                            val deleted = try {
                                dao.deleteNote(note.id)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                -1
                            }

                            withContext(Dispatchers.Main) {
                                if (deleted > 0) {
                                    showText("یادداشت حذف شد")
                                    notes.removeAt(pos)
                                    notifyItemRemoved(pos)
                                } else {
                                    showText("عملیات با مشکل مواجه شد")
                                }
                            }
                        }
                    },
                    negativeAction = {}
                )
            }

            binding.imgRestoreNotes.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                context.showCustomAlert(
                    title = "بازگردانی یادداشت",
                    message = "آیا میخواهید یادداشت بازگردانی شود؟",
                    iconResId = R.drawable.ic_restore,
                    positiveText = "بله",
                    negativeText = "خیر",
                    positiveAction = {
                        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                            val updated = try {
                                dao.updateNoteState(note.id, false)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                -1
                            }

                            withContext(Dispatchers.Main) {
                                if (updated > 0) {
                                    showText("یادداشت بازگردانی شد")
                                    notes.removeAt(pos)
                                    notifyItemRemoved(pos)
                                } else {
                                    showText("عملیات با مشکل مواجه شد")
                                }
                            }
                        }
                    },
                    negativeAction = {}
                )
            }
        }
    }

    private fun showText(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun updateData(newNotes: List<NoteEntity>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in notes.indices) {
            notes.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
