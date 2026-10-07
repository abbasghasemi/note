package ghasemi.abbas.note.ui.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ghasemi.abbas.note.R
import ghasemi.abbas.note.adapters.NotesAdapter
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.data.PrefsManager
import ghasemi.abbas.note.databinding.FragmentNotesBinding
import ghasemi.abbas.note.ui.SharedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), NotesAdapter.OnItemClickListener {
    private val viewModel: NotesViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val notesAdapter = NotesAdapter(this)
    private val selectedIds = mutableSetOf<Long>()
    private var notesSource: LiveData<List<Note>>? = null
    private var showArchived = false
    private lateinit var selectionBackCallback: OnBackPressedCallback

    @Inject lateinit var prefs: PrefsManager

    companion object {
        val snackBar = MutableLiveData("")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBinding.bind(view)

        binding.rvNotes.adapter = notesAdapter
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_notesFragment_to_addFragment)
        }
        binding.rvNotes.setHasFixedSize(true)
        binding.rvNotes.itemAnimator = null
        applyLayoutStyle()
        binding.searchNotes.doAfterTextChanged {
            clearSelection()
            observeNotes()
        }
        binding.archiveFilter.setOnClickListener {
            showArchived = !showArchived
            binding.archiveFilter.setText(if (showArchived) R.string.show_notes else R.string.archive)
            binding.archiveSelected.setText(if (showArchived) R.string.restore_from_archive else R.string.archive)
            clearSelection()
            observeNotes()
        }
        binding.selectAll.setOnClickListener {
            selectedIds.addAll(notesAdapter.NoteList.map(Note::id))
            updateSelection()
        }
        binding.cancelSelection.setOnClickListener { clearSelection() }
        binding.deleteSelected.setOnClickListener { confirmDeleteSelected() }
        binding.archiveSelected.setOnClickListener { archiveSelected() }

        selectionBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = clearSelection()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, selectionBackCallback)

        snackBar.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Snackbar.make(binding.coordinator, message, Snackbar.LENGTH_LONG).show()
                snackBar.value = ""
            }
        }
        setHasOptionsMenu(true)
        observeNotes()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            applyLayoutStyle()
            observeNotes()
        }
    }

    private fun observeNotes() {
        if (_binding == null) return
        notesSource?.removeObservers(viewLifecycleOwner)
        val query = binding.searchNotes.text?.toString()?.trim().orEmpty()
        notesSource = if (query.isEmpty()) viewModel.notes(showArchived)
            else viewModel.searchNote(query, showArchived)
        notesSource?.observe(viewLifecycleOwner) { notes ->
            val items = notes.orEmpty()
            notesAdapter.setData(items)
            selectedIds.retainAll(items.map(Note::id).toSet())
            updateSelection()
            binding.animationView.visibility = if (items.isEmpty() && query.isEmpty()) View.VISIBLE else View.GONE
            binding.tvNoNote.visibility = if (items.isEmpty() && query.isNotEmpty()) View.VISIBLE else View.GONE
            if (items.isEmpty() && query.isEmpty()) binding.animationView.playAnimation()
        }
    }

    private fun applyLayoutStyle() {
        val grid = prefs.getViewStyle() == "grid"
        val current = binding.rvNotes.layoutManager
        if (grid && current !is StaggeredGridLayoutManager) {
            binding.rvNotes.layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            ).apply { gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE }
        } else if (!grid && current !is LinearLayoutManager) {
            binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun updateSelection() {
        if (_binding == null) return
        notesAdapter.setSelection(selectedIds)
        val selecting = selectedIds.isNotEmpty()
        binding.selectionBar.visibility = if (selecting) View.VISIBLE else View.GONE
        binding.fabAdd.visibility = if (selecting || showArchived) View.GONE else View.VISIBLE
        val bottomPadding = ((if (selecting) 120 else 76) * resources.displayMetrics.density).toInt()
        if (binding.rvNotes.paddingBottom != bottomPadding) {
            binding.rvNotes.setPadding(binding.rvNotes.paddingLeft, binding.rvNotes.paddingTop,
                binding.rvNotes.paddingRight, bottomPadding)
        }
        binding.selectedCount.text = getString(R.string.selected_count, selectedIds.size)
        selectionBackCallback.isEnabled = selecting
    }

    private fun clearSelection() {
        selectedIds.clear()
        if (_binding != null) updateSelection()
    }

    private fun confirmDeleteSelected() {
        val selected = notesAdapter.NoteList.filter { it.id in selectedIds }
        if (selected.isEmpty()) return
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_selected_confirmation, selected.size))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_selected) { _, _ ->
                clearSelection()
                viewModel.deleteByIds(selected.map(Note::id)) {
                    val root = _binding?.coordinator ?: return@deleteByIds
                    Snackbar.make(root, R.string.deleted_notes, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) { viewModel.restoreNotes(selected) }
                        .show()
                }
            }
            .show()
    }

    private fun archiveSelected() {
        val ids = selectedIds.toList()
        if (ids.isEmpty()) return
        val archived = !showArchived
        clearSelection()
        viewModel.setArchived(ids, archived) {
            val root = _binding?.coordinator ?: return@setArchived
            Snackbar.make(root, if (archived) R.string.notes_archived else R.string.notes_restored,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) { viewModel.setArchived(ids, !archived) {} }
                .show()
        }
    }

    override fun onFavoriteClicked(markedFavorite: Boolean, id: Long) {
        sharedViewModel.markAsFavorite(markedFavorite, id)
    }

    override fun onNoteClicked(note: Note) {
        if (selectedIds.isNotEmpty()) {
            if (!selectedIds.add(note.id)) selectedIds.remove(note.id)
            updateSelection()
        } else {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToEditFragment(note))
        }
    }

    override fun onNoteLongClicked(note: Note) {
        if (!selectedIds.add(note.id)) selectedIds.remove(note.id)
        updateSelection()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_settings) {
            clearSelection()
            findNavController().navigate(R.id.action_notesFragment_to_settingsFragment)
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        notesSource?.removeObservers(viewLifecycleOwner)
        notesSource = null
        selectedIds.clear()
        _binding = null
        super.onDestroyView()
    }
}
