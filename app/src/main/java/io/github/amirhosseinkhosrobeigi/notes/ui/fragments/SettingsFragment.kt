package io.github.amirhosseinkhosrobeigi.notes.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.databinding.FragmentSettingsBinding
import io.github.amirhosseinkhosrobeigi.notes.utils.BackupUtils
import io.github.amirhosseinkhosrobeigi.notes.utils.ThemeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val exportBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(BackupUtils.getBackupMimeType())
    ) { uri: Uri? ->
        uri?.let {
            exportBackup(it)
        }
    }

    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            importBackup(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSettings()
        setupBackupSettings()
    }

    private fun setupThemeSettings() {

        when (ThemeHelper.getCurrentTheme(requireContext())) {
            ThemeHelper.THEME_SYSTEM -> binding.chipSystem.isChecked = true
            ThemeHelper.THEME_LIGHT -> binding.chipLight.isChecked = true
            ThemeHelper.THEME_DARK -> binding.chipDark.isChecked = true
        }

        binding.themeModeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->

            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val selectedTheme = when (checkedIds.first()) {
                R.id.chipSystem -> ThemeHelper.THEME_SYSTEM
                R.id.chipLight -> ThemeHelper.THEME_LIGHT
                R.id.chipDark -> ThemeHelper.THEME_DARK
                else -> return@setOnCheckedStateChangeListener
            }

            if (selectedTheme != ThemeHelper.getCurrentTheme(requireContext())) {
                ThemeHelper.saveTheme(requireContext(), selectedTheme)
                showThemeChangedMessage()
            }
        }
    }

    private fun setupBackupSettings() {
        binding.btnExportBackup.setOnClickListener {
            val fileName = BackupUtils.createBackupFileName()
            exportBackupLauncher.launch(fileName)
        }

        binding.btnImportBackup.setOnClickListener {
            importBackupLauncher.launch(arrayOf(BackupUtils.getBackupMimeType()))
        }
    }

    private fun exportBackup(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            val success = BackupUtils.exportNotesToJson(requireContext(), uri)
            if (success) {
                BackupUtils.showExportSuccess(requireContext())
            } else {
                BackupUtils.showExportFailed(requireContext())
            }
        }
    }

    private fun importBackup(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            val count = BackupUtils.importNotesFromJson(requireContext(), uri)
            if (count > 0) {
                BackupUtils.showImportSuccess(requireContext(), count)
            } else {
                BackupUtils.showImportFailed(requireContext())
            }
        }
    }

    private fun showThemeChangedMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.theme_restart_required),
            Toast.LENGTH_SHORT
        ).show()
    }
}
