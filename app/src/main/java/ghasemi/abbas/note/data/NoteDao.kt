package ghasemi.abbas.note.data

import androidx.lifecycle.LiveData
import androidx.room.*
import ghasemi.abbas.note.utils.SortBy

@Dao
interface NoteDao {

    fun getNotes(sortBy: String, archived: Boolean): LiveData<List<Note>> {
        return when (sortBy) {
            SortBy.TITLE.colName -> notesSortByTitle(archived)
            SortBy.CREATED_AT.colName -> notesSortByCreatedAt(archived)
            else -> notesSortByLastUpdated(archived)
        }
    }

    fun getNotesFavePinned(sortBy: String, archived: Boolean): LiveData<List<Note>> {
        return when (sortBy) {
            SortBy.TITLE.colName -> notesSortByTitleFavePinned(archived)
            SortBy.CREATED_AT.colName -> notesSortByCreatedAtFavePinned(archived)
            else -> notesSortByLastUpdatedFavePinned(archived)
        }
    }


    @Query("SELECT * FROM notes WHERE archived = :archived ORDER BY title ASC")
    fun notesSortByTitle(archived: Boolean): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE archived = :archived ORDER BY last_updated_at DESC")
    fun notesSortByLastUpdated(archived: Boolean): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE archived = :archived ORDER BY created_at ASC")
    fun notesSortByCreatedAt(archived: Boolean): LiveData<List<Note>>


    @Query("SELECT * FROM notes WHERE archived = :archived ORDER BY favorite DESC, title ASC")
    fun notesSortByTitleFavePinned(archived: Boolean): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE archived = :archived ORDER BY favorite DESC, last_updated_at DESC")
    fun notesSortByLastUpdatedFavePinned(archived: Boolean): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE archived = :archived ORDER BY favorite DESC, created_at ASC")
    fun notesSortByCreatedAtFavePinned(archived: Boolean): LiveData<List<Note>>


    @Query("SELECT * FROM notes WHERE archived = :archived AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%') ORDER BY last_updated_at DESC")
    fun searchNote(searchQuery: String, archived: Boolean): LiveData<List<Note>>

    @Query("UPDATE notes SET archived = :archived WHERE id IN (:ids)")
    fun setArchived(ids: List<Long>, archived: Boolean): Int

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    fun deleteByIds(ids: List<Long>): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(notes: List<Note>)

    @Query("DELETE FROM notes")
    fun clearNotes(): Int

    @Query("UPDATE notes SET favorite = :fave WHERE id = :id")
    fun markAsFavorite(fave: Boolean, id: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note): Long

    @Update
    fun update(note: Note): Int

    @Delete
    fun delete(note: Note): Int

}
