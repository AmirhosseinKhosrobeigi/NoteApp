package io.github.amirhosseinkhosrobeigi.notes.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.data.model.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtils {

    private const val BACKUP_FILE_PREFIX = "notes_backup_"
    private const val BACKUP_FILE_EXTENSION = ".json"
    private const val MIME_TYPE_JSON = "application/json"

    suspend fun exportNotesToJson(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val database = DBHandler.getDatabase(context)
                val notes = database.noteDao().getNotesByState(false).first()
                
                val jsonArray = JSONArray()
                notes.forEach { note ->
                    val jsonObject = JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("detail", note.detail)
                        put("delete_state", note.deleteState)
                        put("date", note.date)
                        put("is_favorite", note.isFavorite)
                    }
                    jsonArray.put(jsonObject)
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(jsonArray.toString(2))
                        writer.flush()
                    }
                }
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importNotesFromJson(context: Context, uri: Uri): Int {
        return withContext(Dispatchers.IO) {
            try {
                val database = DBHandler.getDatabase(context)
                var importedCount = 0
                
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val jsonString = reader.readText()
                        val jsonArray = JSONArray(jsonString)
                        
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            
                            val note = NoteEntity(
                                id = jsonObject.optInt("id", 0),
                                title = jsonObject.getString("title"),
                                detail = jsonObject.getString("detail"),
                                deleteState = jsonObject.optBoolean("delete_state", false),
                                date = jsonObject.getString("date"),
                                isFavorite = jsonObject.optBoolean("is_favorite", false)
                            )
                            
                            // Insert note (Room will handle duplicate IDs by auto-generating new ones)
                            database.noteDao().insertNote(note)
                            importedCount++
                        }
                    }
                }
                
                importedCount
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    fun createBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
    }

    fun getBackupMimeType(): String = MIME_TYPE_JSON

    fun showExportSuccess(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.backup_export_success),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showExportFailed(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.backup_export_failed),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showImportSuccess(context: Context, count: Int) {
        Toast.makeText(
            context,
            context.getString(R.string.backup_import_success, count),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showImportFailed(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.backup_import_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
}
