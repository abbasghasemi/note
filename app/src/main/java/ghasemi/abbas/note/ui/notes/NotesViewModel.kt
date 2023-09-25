package ghasemi.abbas.note.ui.notes

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.data.NoteDao
import ghasemi.abbas.note.data.NoteDatabase
import ghasemi.abbas.note.data.PrefsManager
import ghasemi.abbas.note.di.DatabaseController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val noteDao: NoteDao,
) : ViewModel() {

    private val TAG = NotesViewModel::class.java.simpleName

    lateinit var searchQuery: String

    val allNotes: LiveData<List<Note>>
        get() = getNotes()

    private fun getNotes(): LiveData<List<Note>> {
        Log.d(TAG, "getNotes: ${prefs.sortBy()}")
        return if (prefs.favoritePinnedStatus()) {
            _noteDao.getNotesFavePinned(prefs.sortBy().toString())
        } else {
            _noteDao.getNotes(prefs.sortBy().toString())
        }
    }

    fun searchNote(searchQuery: String): LiveData<List<Note>> = _noteDao.searchNote(searchQuery)

    fun deleteAllNotes() = viewModelScope.launch(Dispatchers.IO) { _noteDao.clearNotes() }

    //TODO
    private val _noteDao: NoteDao
        get() = DatabaseController.database!!.noteDao()

    fun openDatabase(applicationContext: Context) {
        DatabaseController.database = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "data"
        ).fallbackToDestructiveMigration().build()
    }

    fun closeDatabase() {
        DatabaseController.database!!.close()
    }

}