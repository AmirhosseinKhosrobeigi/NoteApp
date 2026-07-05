package io.github.amirhosseinkhosrobeigi.notes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.amirhosseinkhosrobeigi.notes.data.local.dao.NoteDao
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = DBHandler.DB_VERSION
)
abstract class DBHandler : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        private const val DB_NAME = "Notes"
        const val DB_VERSION = 1

        const val NOTE_TABLE = "noteTable"

        @Volatile
        private var INSTANCE: DBHandler? = null

        fun getDatabase(context: Context): DBHandler {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DBHandler::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
