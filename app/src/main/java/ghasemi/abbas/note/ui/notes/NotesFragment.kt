package ghasemi.abbas.note.ui.notes

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import ghasemi.abbas.note.di.DatabaseController
import ghasemi.abbas.note.ui.SharedViewModel
import ghasemi.abbas.note.utils.SortBy
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), NotesAdapter.OnItemClickListener {

    private val TAG = NotesFragment::class.java.simpleName
    private val viewModel: NotesViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private var _binding: FragmentNotesBinding? = null
    private val binding
        get() = _binding
    private val notesAdapter = NotesAdapter(this)

    @Inject
    lateinit var prefs: PrefsManager

    companion object {
        val snackBar = MutableLiveData("")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO
        if (DatabaseController.database != null && !DatabaseController.database!!.isOpen) {
            viewModel.openDatabase(requireContext())
        }
        _binding = FragmentNotesBinding.bind(view)

        binding?.apply {
            rvNotes.apply {
                adapter = notesAdapter
                layoutStyle()
                setHasFixedSize(true)
            }
        }

        viewModel.allNotes.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                notesAdapter.setData(it)
            } else binding!!.apply {
                tvNoNote.visibility = View.GONE
                animationView.isVisible = true
                animationView.playAnimation()
            }
        }
        Log.d(TAG, "onViewCreated:${prefs.getViewStyle()}")

        snackBar.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showSnackBar(it)
                snackBar.value = ""
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_notes, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        val pinFavoriteMenu = menu.findItem(R.id.action_pin_favorites)
        pinFavoriteMenu.isChecked = prefs.favoritePinnedStatus()

        when (prefs.sortBy()) {
            SortBy.TITLE.colName -> updateMenu(menu.findItem(R.id.action_sort_by_title))
            SortBy.CREATED_AT.colName -> updateMenu(menu.findItem(R.id.action_sort_by_date_created))
            SortBy.LAST_UPDATED_AT.colName -> updateMenu(menu.findItem(R.id.action_sort_by_date_modified))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(searchQuery: String): Boolean {
                //viewModel.searchQuery = searchQuery
                searchNote(searchQuery)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_pin_favorites -> {
                prefs.favoritePinned(!prefs.favoritePinnedStatus())
                viewModel.allNotes.observe(this) { notesAdapter.setData(it) }
                item.isChecked = prefs.favoritePinnedStatus()
                true
            }

            R.id.action_sort_by_title -> {
                prefs.setSortBy(SortBy.TITLE.colName)
                updateMenu(item)
                true
            }

            R.id.action_sort_by_date_created -> {
                prefs.setSortBy(SortBy.CREATED_AT.colName)
                updateMenu(item)
                true
            }

            R.id.action_sort_by_date_modified -> {
                prefs.setSortBy(SortBy.LAST_UPDATED_AT.colName)
                updateMenu(item)
                true
            }

            R.id.action_settings -> {
                findNavController().navigate(R.id.action_notesFragment_to_settingsFragment)
                true
            }

            R.id.action_delete_all_notes -> {
                deleteAllDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMenu(item: MenuItem) {
        viewModel.allNotes.observe(this) {
            notesAdapter.setData(it)
            binding!!.tvNoNote.visibility = View.GONE
        }
        item.isChecked = true
    }

    private fun deleteAllDialog() {

        if (notesAdapter.NoteList.isNotEmpty()) {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setPositiveButton("بله") { _, _ ->

                val notes: List<Note> = notesAdapter.NoteList
                viewModel.deleteAllNotes()

                showSnackBar(
                    "همه یادداشت ها حذف شد.",
                    "بازگشت"
                ) {
                    for (note in notes) {
                        sharedViewModel.insertNote(note)
                        binding!!.animationView.isVisible = false
                    }
                }
            }
            builder.setNegativeButton("خیر") { _, _ -> }
            builder.setTitle("حذف یادداشت ها")
            builder.setMessage("آیا می خواهید همه چیز را حذف کنید؟")
            builder.create().show()
        } else {
            showSnackBar("یادداشتی برای حذف وجود ندارد.")
        }
    }

    private fun searchNote(query: String) {
        val searchQuery = "%${query}%"
        viewModel.searchNote(searchQuery).observe(this) { list ->
            list?.let {
                if (it.isEmpty()) {
                    binding!!.rvNotes.visibility = View.GONE
                    binding!!.tvNoNote.visibility = View.VISIBLE
                } else {
                    binding!!.tvNoNote.visibility = View.GONE
                    binding!!.rvNotes.visibility = View.VISIBLE
                    notesAdapter.setData(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewModel.allNotes.removeObservers(viewLifecycleOwner)
        snackBar.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.allNotes.observe(viewLifecycleOwner) { notesAdapter.setData(it) }
    }

    private fun layoutStyle() {
        if (prefs.getViewStyle().equals("list")) {
            binding!!.rvNotes.layoutManager =
                LinearLayoutManager(context)
        } else {
            binding!!.rvNotes.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    override fun onFavoriteClicked(markedFavorite: Boolean, id: Long) {
        sharedViewModel.markAsFavorite(markedFavorite, id)
    }

    private fun showSnackBar(
        msg: String,
        btnName: String? = null,
        listener: View.OnClickListener? = null
    ) {
        val snackBar = Snackbar.make(
            binding!!.coordinator, msg,
            Snackbar.LENGTH_LONG
        )
        if (btnName != null) {
            snackBar.setAction(btnName, listener)
        }
        snackBar.show()
    }

    override fun onDestroy() {
        viewModel.closeDatabase()
        super.onDestroy()
    }
}