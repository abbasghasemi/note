package ghasemi.abbas.note.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ghasemi.abbas.note.data.Note
import ghasemi.abbas.note.data.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val noteDao: NoteDao) : ViewModel() {

    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) { noteDao.insert(note) }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) { noteDao.update(note) }

    fun deleteItem(note: Note) = viewModelScope.launch(Dispatchers.IO) { noteDao.delete(note) }

    fun markAsFavorite(fave: Boolean, id: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.markAsFavorite(fave, id)
        }
}
