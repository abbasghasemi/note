package ghasemi.abbas.note.ui.notes

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.data.NoteDao
import ghasemi.abbas.note.data.NoteDatabase
import ghasemi.abbas.note.data.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val noteDao: NoteDao,
    private val database: NoteDatabase,
) : ViewModel() {

    private val TAG = NotesViewModel::class.java.simpleName

    fun notes(archived: Boolean): LiveData<List<Note>> = getNotes(archived)

    private fun getNotes(archived: Boolean): LiveData<List<Note>> {
        Log.d(TAG, "getNotes: ${prefs.sortBy()}")
        return if (prefs.favoritePinnedStatus()) {
            noteDao.getNotesFavePinned(prefs.sortBy().toString(), archived)
        } else {
            noteDao.getNotes(prefs.sortBy().toString(), archived)
        }
    }

    fun searchNote(searchQuery: String, archived: Boolean): LiveData<List<Note>> =
        noteDao.searchNote(searchQuery, archived)

    fun setArchived(ids: List<Long>, archived: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        withContext(Dispatchers.IO) { noteDao.setArchived(ids, archived) }
        onDone()
    }

    fun deleteByIds(ids: List<Long>, onDeleted: () -> Unit) = viewModelScope.launch {
        withContext(Dispatchers.IO) { noteDao.deleteByIds(ids) }
        onDeleted()
    }

    fun restoreNotes(notes: List<Note>) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.insertAll(notes)
    }

    fun closeDatabase() = database.close()

    fun openDatabase() {
        database.openHelper.writableDatabase
    }

}
