package ghasemi.abbas.note.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.data.NoteDao
import ghasemi.abbas.note.di.DatabaseController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val noteDao: NoteDao) : ViewModel() {

    //TODO
    private val _noteDao: NoteDao
        get() = DatabaseController.database!!.noteDao()

    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) { _noteDao.insert(note) }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) { _noteDao.update(note) }

    fun deleteItem(note: Note) = viewModelScope.launch(Dispatchers.IO) { _noteDao.delete(note) }

    fun markAsFavorite(fave: Boolean, id: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            _noteDao.markAsFavorite(
                fave,
                System.currentTimeMillis(),
                id
            )
        }
}