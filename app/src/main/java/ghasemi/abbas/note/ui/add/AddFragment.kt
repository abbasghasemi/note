package ghasemi.abbas.note.ui.add

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ghasemi.abbas.note.R
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.databinding.FragmentDetailBinding
import ghasemi.abbas.note.ui.SharedViewModel
import ghasemi.abbas.note.ui.setupEditorTools
import ghasemi.abbas.note.ui.notes.NotesFragment
import ghasemi.abbas.note.utils.HideKeyboard.Companion.hideKeyboard
import java.util.Locale

@AndroidEntryPoint
class AddFragment : Fragment(R.layout.fragment_detail) {
    private var savedNote = false
    private val viewModel: SharedViewModel by viewModels()
    private var _binding: FragmentDetailBinding? = null
    private val binding
        get() = _binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)

        binding!!.apply {
            tvNoteDate.isVisible = false
            background.setBackgroundColor(-1)
            setupEditorTools()
            if (savedInstanceState == null) {
                val draft = requireContext().getSharedPreferences("new_note_draft", Context.MODE_PRIVATE)
                val draftTitle = draft.getString("title", "").orEmpty()
                val draftContent = draft.getString("content", "").orEmpty()
                if (draftTitle.isNotEmpty() || draftContent.isNotEmpty()) {
                    etAddTitle.setText(draftTitle)
                    etAddNote.setText(draftContent)
                    val draftColor = draft.getInt("color", -1)
                    colorSlider.selectColor(draftColor)
                    background.setBackgroundColor(draftColor)
                    Snackbar.make(root, R.string.draft_restored, Snackbar.LENGTH_SHORT).show()
                }
            }
            colorSlider.setListener { index, color ->
                background.setBackgroundColor(color)
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
            Snackbar.make(requireView(), "دستگاه شما پشتیبانی نمی کند.", Snackbar.LENGTH_LONG).show()
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            data?.let {
                binding!!.etAddNote.text = binding?.etAddNote?.text?.append(it[0])
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> { addNewNote(); return true }
            android.R.id.home -> hideKeyboard()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addNewNote() {
        val title = binding!!.etAddTitle.text.toString()
        val note = binding!!.etAddNote.text.toString()
        val color = binding!!.colorSlider.selectedColor
        Log.d("XO", "$color")
        if (title.isBlank() && note.isBlank()) {
            Snackbar.make(
                requireView(),
                "لطفا عنوان یا شرح یادداشت را پر کنید.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            val newNote = Note(title = title, content = note, bgColor = color)
            viewModel.insertNote(newNote)
            savedNote = true
            requireContext().getSharedPreferences("new_note_draft", Context.MODE_PRIVATE)
                .edit().clear().apply()
            findNavController().popBackStack();
            NotesFragment.snackBar.value = "'$title' ذخیره شد."
        }
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        if (!savedNote) {
            binding?.let { editor ->
                val title = editor.etAddTitle.text?.toString().orEmpty()
                val content = editor.etAddNote.text?.toString().orEmpty()
                val draft = requireContext().getSharedPreferences("new_note_draft", Context.MODE_PRIVATE)
                if (title.isBlank() && content.isBlank()) draft.edit().clear().apply()
                else draft.edit().putString("title", title).putString("content", content)
                    .putInt("color", editor.colorSlider.selectedColor).apply()
            }
        }
        super.onPause()
    }
}
