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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): NoteDatabase {
        return Room.databaseBuilder(
                applicationContext,
                NoteDatabase::class.java,
                "data"
        ).build()
    }

    @Provides
    fun provideNoteDao(noteDatabase: NoteDatabase): NoteDao = noteDatabase.noteDao()
}
