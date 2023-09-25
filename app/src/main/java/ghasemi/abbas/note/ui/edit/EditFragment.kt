package ghasemi.abbas.note.ui.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ghasemi.abbas.note.R
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.databinding.FragmentDetailBinding
import ghasemi.abbas.note.ui.SharedViewModel
import ghasemi.abbas.note.ui.notes.NotesFragment
import ghasemi.abbas.note.utils.HideKeyboard.Companion.hideKeyboard
import java.util.*

@AndroidEntryPoint
class EditFragment : Fragment(R.layout.fragment_detail) {
    private val args: EditFragmentArgs by navArgs()
    private val viewModel: SharedViewModel by viewModels()
    private var _binding: FragmentDetailBinding? = null
    private val binding
        get() = _binding

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)

        binding?.apply {
            etAddTitle.setText(args.currentItem.title)
            etAddNote.setText(args.currentItem.content)
            tvNoteDate.text =
                "${getString(R.string.date_modified)}: ${args.currentItem.lastUpdatedAtFormatted}"
            colorSlider.selectColor(args.currentItem.bgColor)
            background.setBackgroundColor(args.currentItem.bgColor)
            colorSlider.setListener { index, color ->
                background.setBackgroundColor(color)
//                if (index in 1..3) {
//                    binding!!.etAddTitle.setTextColor(Color.WHITE)
//                    binding!!.etAddNote.setTextColor(Color.WHITE)
//                    binding!!.tvNoteDate.setTextColor(Color.WHITE)
//                } else {
//                    binding!!.etAddTitle.setTextColor(Color.BLACK)
//                    binding!!.etAddNote.setTextColor(Color.BLACK)
//                    binding!!.tvNoteDate.setTextColor(Color.BLACK)
//                }
            }
            fabStt.setOnClickListener { openSTTActivity() }
        }
        setHasOptionsMenu(true)
    }

    private fun openSTTActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("fa").language)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_to_text_button))
        try {
            resultLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(requireView(), "دستگاه شما پشتیبانی نمی کند.", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                data?.let {
                    binding!!.etAddNote.text = binding?.etAddNote?.text?.append(it[0])
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_edit, menu)
        val item: MenuItem = menu.findItem(R.id.menu_favorite)
        if (args.currentItem.favorite) {
            item.title = getString(R.string.remove_from_favorites)
        } else {
            item.title = getString(R.string.add_to_favorites)
        }
        when (item.itemId) {
            android.R.id.home -> hideKeyboard()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_update -> updateNote()
            R.id.menu_delete -> deleteNote()
            R.id.menu_favorite -> {
                markAsFavorite()
                if (args.currentItem.favorite) {
                    item.title = getString(R.string.remove_from_favorites)
                } else {
                    item.title = getString(R.string.add_to_favorites)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun markAsFavorite() {
        viewModel.markAsFavorite(!args.currentItem.favorite, args.currentItem.id)
        args.currentItem.favorite = !args.currentItem.favorite
    }

    private fun deleteNote() {
        val title = binding?.etAddTitle?.text.toString()

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton("بله") { _, _ ->
            viewModel.deleteItem(args.currentItem)
            findNavController().popBackStack();
            NotesFragment.snackBar.value = "'$title' حذف شد."
        }
        builder.setNegativeButton("خیر") { _, _ -> }
        builder.setTitle("حذف '$title'؟")
        builder.setMessage("آیا می خواهید '$title' حذف کنید؟")
        builder.create().show()
    }

    private fun updateNote() {
        val title = binding!!.etAddTitle.text.toString()
        val note = binding!!.etAddNote.text.toString()
        val color = binding!!.colorSlider.selectedColor
        if (title.isEmpty() && note.isEmpty()) {
            Snackbar.make(
                requireView(),
                "لطفا عنوان یا شرح یادداشت را پر کنید.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            val newNote = Note(
                args.currentItem.id,
                title,
                note,
                args.currentItem.favorite,
                color,
                false,
                System.currentTimeMillis(),
                args.currentItem.createdAt
            )
            viewModel.updateNote(newNote)
            findNavController().popBackStack();
            NotesFragment.snackBar.value = "'$title' ویرایش شد."
        }
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}