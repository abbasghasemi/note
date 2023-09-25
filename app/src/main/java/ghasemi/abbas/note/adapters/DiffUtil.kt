package ghasemi.abbas.note.adapters

import androidx.recyclerview.widget.DiffUtil
import ghasemi.abbas.note.data.Note

class DiffUtil(private val oldList: List<Note>, private val newList: List<Note>) :
    DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldItemPosition == newItemPosition

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id &&
                oldList[oldItemPosition].title == newList[newItemPosition].title &&
                oldList[oldItemPosition].content == newList[newItemPosition].content &&
                oldList[oldItemPosition].favorite == newList[newItemPosition].favorite
    }
}