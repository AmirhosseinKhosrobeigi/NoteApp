package io.github.amirhosseinkhosrobeigi.notes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        const val DB_VERSION = 2

        const val NOTE_TABLE = "noteTable"

        @Volatile
        private var INSTANCE: DBHandler? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE $NOTE_TABLE ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): DBHandler {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DBHandler::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
