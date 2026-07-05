package io.github.amirhosseinkhosrobeigi.notes.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler

@Entity(tableName = DBHandler.NOTE_TABLE)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "detail") val detail: String,
    @ColumnInfo(name = "delete_state") val deleteState: Boolean = false,
    @ColumnInfo(name = "date") val date: String
)

