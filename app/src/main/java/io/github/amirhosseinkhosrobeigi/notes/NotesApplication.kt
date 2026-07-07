package io.github.amirhosseinkhosrobeigi.notes

import android.app.Application
import io.github.amirhosseinkhosrobeigi.notes.utils.ThemeHelper

class NotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ThemeHelper.initializeTheme(this)
    }
}
