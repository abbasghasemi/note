package ghasemi.abbas.note.data

import androidx.lifecycle.LiveData
import androidx.room.*
import ghasemi.abbas.note.utils.SortBy

@Dao
interface NoteDao {

    fun getNotes(sortBy: String): LiveData<List<Note>> {
        return when (sortBy) {
            SortBy.TITLE.colName -> notesSortByTitle()
            SortBy.CREATED_AT.colName -> notesSortByCreatedAt()
            else -> notesSortByLastUpdated()
        }
    }

    fun getNotesFavePinned(sortBy: String): LiveData<List<Note>> {
        return when (sortBy) {
            SortBy.TITLE.colName -> notesSortByTitleFavePinned()
            SortBy.CREATED_AT.colName -> notesSortByCreatedAtFavePinned()
            else -> notesSortByLastUpdatedFavePinned()
        }
    }


    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun notesSortByTitle(): LiveData<List<Note>>

    @Query("SELECT * FROM notes ORDER BY last_updated_at DESC")
    fun notesSortByLastUpdated(): LiveData<List<Note>>

    @Query("SELECT * FROM notes ORDER BY created_at ASC")
    fun notesSortByCreatedAt(): LiveData<List<Note>>


    @Query("SELECT * FROM notes ORDER BY favorite DESC, title ASC")
    fun notesSortByTitleFavePinned(): LiveData<List<Note>>

    @Query("SELECT * FROM notes ORDER BY favorite DESC, last_updated_at DESC")
    fun notesSortByLastUpdatedFavePinned(): LiveData<List<Note>>

    @Query("SELECT * FROM notes ORDER BY favorite DESC, created_at ASC")
    fun notesSortByCreatedAtFavePinned(): LiveData<List<Note>>


    @Query("SELECT * FROM notes WHERE title LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    fun searchNote(searchQuery: String): LiveData<List<Note>>

    @Query("DELETE FROM notes")
    fun clearNotes(): Int

    @Query("UPDATE notes SET favorite = :fave, last_updated_at = :time WHERE id = :id")
    fun markAsFavorite(fave: Boolean, time: Long, id: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note): Long

    @Update
    fun update(note: Note): Int

    @Delete
    fun delete(note: Note): Int

}