package ghasemi.abbas.note.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ghasemi.abbas.note.data.NoteDao
import ghasemi.abbas.note.data.NoteDatabase
import javax.inject.Singleton

class DatabaseController {
    companion object {
        var database: NoteDatabase? = null
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): NoteDatabase {
        DatabaseController.database = Room.databaseBuilder(
                applicationContext,
                NoteDatabase::class.java,
                "data"
        ).fallbackToDestructiveMigration().build()
        return DatabaseController.database!!
    }

    @Provides
    fun provideNoteDao(noteDatabase: NoteDatabase): NoteDao = noteDatabase.noteDao()
}