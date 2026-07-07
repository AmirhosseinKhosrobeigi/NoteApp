package io.github.amirhosseinkhosrobeigi.notes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.amirhosseinkhosrobeigi.notes.adapter.recycler.RecycleBinAdapter
import io.github.amirhosseinkhosrobeigi.notes.data.local.DBHandler
import io.github.amirhosseinkhosrobeigi.notes.databinding.FragmentRecycleBinBinding
import kotlinx.coroutines.launch

class RecycleBinFragment : Fragment() {

    private lateinit var binding: FragmentRecycleBinBinding
    private lateinit var adapter: RecycleBinAdapter
    private lateinit var db: DBHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecycleBinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHandler.getDatabase(requireContext())

        adapter = RecycleBinAdapter(
            context = requireContext(),
            dao = db.noteDao(),
            notes = arrayListOf()
        )

        binding.recyclerNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotes.adapter = adapter

        lifecycleScope.launch {
            db.noteDao().getNotesByState(true)
                .collect { notes ->
                    adapter.updateData(notes)
                }
        }
    }
}
