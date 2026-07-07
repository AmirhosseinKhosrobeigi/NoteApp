package io.github.amirhosseinkhosrobeigi.notes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.github.amirhosseinkhosrobeigi.notes.R
import io.github.amirhosseinkhosrobeigi.notes.databinding.FragmentSettingsBinding
import io.github.amirhosseinkhosrobeigi.notes.utils.ThemeHelper

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSettings()
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

    private fun showThemeChangedMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.theme_restart_required),
            Toast.LENGTH_SHORT
        ).show()
    }
}
