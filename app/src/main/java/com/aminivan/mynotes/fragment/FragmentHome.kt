package com.aminivan.mynotes.fragment

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminivan.mynotes.R
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.NoteRoomDatabase
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.CustomDialogBinding
import com.aminivan.mynotes.databinding.FragmentHomeBinding
import com.aminivan.mynotes.helper.DateHelper
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.aminivan.mynotes.viewmodel.NoteAdapter

class FragmentHome : Fragment() {

    lateinit var binding : FragmentHomeBinding
    lateinit var dialogBinding: CustomDialogBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var dataUserShared : SharedPreferences

    private var note: Note? = null
    private var user : User? = null
    private lateinit var adapter: NoteAdapter

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
        var context = binding.rvNotes.context
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        note = Note()
        user = User()
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)
        getData()

        setAdapter()

        binding.fabAdd.setOnClickListener(){
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog)
            val judul : EditText = dialog.findViewById(R.id.edtJudul)
            val catatan : EditText = dialog.findViewById(R.id.edtCatatan)
            val submit : Button = dialog.findViewById(R.id.btnSubmit)

            submit.setOnClickListener{
                when {
                    judul.text.toString().isEmpty() -> {
                        dialogBinding.edtJudul.error = "Data Tidak Boleh Kosong !!"
                    }
                    catatan.text.toString().isEmpty() -> {
                        dialogBinding.edtCatatan.error = "Data Tidak Boleh Kosong !!"
                    }

                    else -> {
                        note.let { note ->
                            note?.title = judul.text.toString()
                            note?.description = catatan.text.toString()
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

    fun getData(){
        user.let { user ->
            user?.id = Integer.parseInt(dataUserShared.getString("id",""))
            user?.username = dataUserShared.getString("username","")
            user?.email = dataUserShared.getString("email","")
            user?.password = dataUserShared.getString("password","")
        }

        binding.tvWelcomeHome.setText("Welcome , ${user?.username} !")
    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun setAdapter(){
        adapter = NoteAdapter(
            object : NoteAdapter.OnAdapterListener {
                override fun onDelete(note: Note) {
                    noteAddUpdateViewModel.delete(note)
                    Toast.makeText(context, "${note} DELETED", Toast.LENGTH_SHORT).show()
                    observer()
                }

                override fun onUpdate(note: Note) {
                    noteAddUpdateViewModel.update(note)
                    Toast.makeText(context, "Notes updated", Toast.LENGTH_SHORT).show()
                    observer()
                }
            }
        )
        binding?.rvNotes?.layoutManager = LinearLayoutManager(context)
        binding?.rvNotes?.setHasFixedSize(true)
        binding?.rvNotes?.adapter = adapter

        observer()
    }
    fun observer(){
        val mainViewModel = obtainViewModel(requireActivity())
        mainViewModel.getAllNotes().observe(requireActivity(), { noteList ->
            if (noteList != null) {
                adapter.setListNotes(noteList)
            }
        })
    }
}