package io.github.amirhosseinkhosrobeigi.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM ${DBHandler.NOTE_TABLE} WHERE delete_state = :state")
    fun getNotesByState(state: Boolean): Flow<List<NoteEntity>>

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query("UPDATE ${DBHandler.NOTE_TABLE} SET delete_state = :state WHERE id = :id")
    suspend fun updateNoteState(id: Int, state: Boolean): Int

    @Query("SELECT id, title, detail, delete_state, date FROM ${DBHandler.NOTE_TABLE} WHERE delete_state = :state")
    fun getNotesForRecycler(state: Boolean): Flow<List<NoteEntity>>

    @Query("SELECT * FROM ${DBHandler.NOTE_TABLE} WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Query("DELETE FROM ${DBHandler.NOTE_TABLE} WHERE id = :id")
    suspend fun deleteNote(id: Int): Int
}
