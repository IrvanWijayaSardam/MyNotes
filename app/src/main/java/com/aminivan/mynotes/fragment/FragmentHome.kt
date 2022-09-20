package com.aminivan.mynotes.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.aminivan.mynotes.R
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.NoteRoomDatabase
import com.aminivan.mynotes.databinding.CustomDialogBinding
import com.aminivan.mynotes.databinding.FragmentHomeBinding
import com.aminivan.mynotes.helper.DateHelper
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory

class FragmentHome : Fragment() {

    lateinit var binding : FragmentHomeBinding
    lateinit var dialogBinding: CustomDialogBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel

    private var note: Note? = null
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        note = Note()
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        binding.fabAdd.setOnClickListener(){
            var context = binding.rvNotes.context
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog)
            val judul = "Test"
            val catatan = "test"
            val submit : Button = dialog.findViewById(R.id.btnSubmit)

            submit.setOnClickListener{
                when {
                    judul.isEmpty() -> {
                        Toast.makeText(context, "Jduul Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
                    }
                    catatan.isEmpty() -> {
                        Toast.makeText(context, "catatan Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        note.let { note ->
                            note?.title = judul
                            note?.description = catatan
                            note?.date = DateHelper.getCurrentDate()
                        }
                        noteAddUpdateViewModel.insert(note as Note)
                        Toast.makeText(context, "Berhasil menambahkan satu data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.show()
        }
    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

}