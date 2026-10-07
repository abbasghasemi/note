package ghasemi.abbas.note.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.drawable.ColorDrawable
import androidx.core.view.isVisible
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.databinding.ItemNotesBinding
import ghasemi.abbas.note.R

class NotesAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    init { setHasStableIds(true) }

    var NoteList = emptyList<Note>()
        private set
    private var selectedIds = emptySet<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNotesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = NoteList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return NoteList.size
    }

    override fun getItemId(position: Int): Long = NoteList[position].id

    fun setData(notesData: List<Note>) {
        if (NoteList == notesData) return
        val notesDiffUtil = DiffUtil(NoteList, notesData)
        val notesDiffResult = DiffUtil.calculateDiff(notesDiffUtil)

        this.NoteList = notesData
        notesDiffResult.dispatchUpdatesTo(this)
    }

    fun setSelection(ids: Set<Long>) {
        val next = ids.toSet()
        if (selectedIds == next) return
        val wasSelecting = selectedIds.isNotEmpty()
        val isSelecting = next.isNotEmpty()
        val changedIds = selectedIds.symmetricDifference(next)
        selectedIds = next
        if (wasSelecting != isSelecting) notifyItemRangeChanged(0, itemCount)
        else NoteList.forEachIndexed { index, note ->
            if (note.id in changedIds) notifyItemChanged(index)
        }
    }

    private fun <T> Set<T>.symmetricDifference(other: Set<T>): Set<T> =
        (this - other) + (other - this)

    inner class NoteViewHolder(private val binding: ItemNotesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.note = note
            binding.ivFavorite.setImageResource(
                if (note.favorite) R.drawable.round_star_24 else R.drawable.round_star_border_24
            )
            binding.ivFavorite.visibility = if (selectedIds.isEmpty()) View.VISIBLE else View.INVISIBLE
            binding.selectionCheckbox.isVisible = selectedIds.isNotEmpty()
            binding.selectionCheckbox.isChecked = note.id in selectedIds
            binding.selectionCheckbox.setOnClickListener { listener.onNoteClicked(note) }
            binding.root.foreground = ColorDrawable(
                if (note.id in selectedIds) 0x222563EB else android.graphics.Color.TRANSPARENT
            )
            binding.root.setOnClickListener { listener.onNoteClicked(note) }
            binding.root.setOnLongClickListener {
                listener.onNoteLongClicked(note)
                true
            }
            binding.ivFavorite.setOnClickListener {
                if (selectedIds.isNotEmpty()) listener.onNoteClicked(note)
                else listener.onFavoriteClicked(!note.favorite, note.id)
            }

            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onFavoriteClicked(markedFavorite: Boolean, id: Long)
        fun onNoteClicked(note: Note)
        fun onNoteLongClicked(note: Note)
    }
}
