package ghasemi.abbas.note.adapters

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import ghasemi.abbas.note.R
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.ui.notes.NotesFragmentDirections

class BindingAdapter {
    companion object {
        @BindingAdapter("android:nav_to_add")
        @JvmStatic
        fun navToAddFragment(view: ExtendedFloatingActionButton, navigate: Boolean) {
            view.setOnClickListener {
                if (navigate) {
                    view.findNavController().navigate(R.id.action_notesFragment_to_addFragment)
                }
            }
        }

        @BindingAdapter("android:is_favorite")
        @JvmStatic
        fun isFavorite(img: ImageView, marked: Boolean) {
            if (marked) {
                img.setImageResource(R.drawable.round_star_24)
            } else img.setImageResource(R.drawable.round_star_border_24)
        }

        @BindingAdapter("android:send_notes_to_edit")
        @JvmStatic
        fun sendNotesToEdit(view: ConstraintLayout, currentItem: Note) {
            view.setOnClickListener {
                val action = NotesFragmentDirections.actionNotesFragmentToEditFragment(currentItem)
                view.findNavController().navigate(action)
            }
        }
    }
}