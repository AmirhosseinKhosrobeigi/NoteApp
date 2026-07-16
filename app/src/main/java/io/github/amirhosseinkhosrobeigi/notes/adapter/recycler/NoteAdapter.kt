package io.github.amirhosseinkhosrobeigi.notes.adapter.recycler

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import io.github.amirhosseinkhosrobeigi.notes.databinding.ListItemNotesBinding

class NoteAdapter(
    private var data: ArrayList<NoteEntity>,
    private val onDeleteClick: (NoteEntity, Int) -> Unit,
    private val onItemClick: (NoteEntity) -> Unit,
    private val onFavoriteClick: (NoteEntity) -> Unit = { _ -> }
) : RecyclerView.Adapter<NoteAdapter.NotesViewHolder>() {

    inner class SwipeToDeleteCallback(
        private val adapter: NoteAdapter,
        private val onSwipeDelete: (NoteEntity, Int) -> Unit
    ) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private val deleteIcon: Drawable? by lazy {
            ContextCompat.getDrawable(adapter.context, R.drawable.ic_delete)
        }
        private val intrinsicWidth: Int by lazy {
            deleteIcon?.intrinsicWidth ?: 0
        }
        private val intrinsicHeight: Int by lazy {
            deleteIcon?.intrinsicHeight ?: 0
        }
        private val background: ColorDrawable by lazy {
            ColorDrawable(Color.parseColor("#80FF5252"))
        }
        private val backgroundColor: Int by lazy {
            Color.parseColor("#80FF5252")
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onSwipeDelete(data[position], position)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            // Draw delete icon
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            deleteIcon?.let {
                it.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                it.draw(c)
            }
        }
    }

    private lateinit var context: android.content.Context

    inner class NotesViewHolder(
        private val binding: ListItemNotesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setDataNote(note: NoteEntity) {
            binding.txtTitleNotes.text = note.title

            val starIcon = if (note.isFavorite) {
                R.drawable.ic_star
            } else {
                R.drawable.ic_star_outline
            }
            binding.imgStarNotes.setImageResource(starIcon)
            binding.imgStarNotes.clearColorFilter()

            binding.imgStarNotes.setOnClickListener {
                onFavoriteClick(note)
            }

            binding.imgDeleteNotesRecycler.setOnClickListener {
                onDeleteClick(note, layoutPosition)
            }

            binding.root.setOnClickListener {
                onItemClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        context = parent.context
        return NotesViewHolder(
            ListItemNotesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.setDataNote(data[position])
    }

    fun updateData(newData: List<NoteEntity>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    fun attachSwipeToDelete(
        recyclerView: RecyclerView,
        onSwipeDelete: (NoteEntity, Int) -> Unit
    ): ItemTouchHelper {
        val callback = SwipeToDeleteCallback(this, onSwipeDelete)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        return itemTouchHelper
    }
}
