package com.aminivan.mynotes.fragment

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.CustomDialogBinding
import com.aminivan.mynotes.databinding.FragmentHomeBinding
import com.aminivan.mynotes.helper.DateHelper
import com.aminivan.mynotes.helper.SwipeToDeleteCallback
import com.aminivan.mynotes.helper.URIPathHelper
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.PostNotesResponse
import com.aminivan.mynotes.viewmodel.NoteAdapter
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.log


class FragmentHome : Fragment() {

    lateinit var binding : FragmentHomeBinding
    lateinit var dialogBinding: CustomDialogBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var dataUserShared : SharedPreferences
    lateinit var selectedFile : String
    lateinit var dialog : Dialog

    private val pickImage = 100
    lateinit var imageUri : Uri

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

    override fun onResume() {
        super.onResume()
        val tvAttachImage : TextView = dialog.findViewById(R.id.tvAttachFile)
        tvAttachImage.text = selectedFile
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var context = binding.rvNotes.context
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectedFile = "Attach File"

        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        note = Note()
        user = User()
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)
        getData()
        setAdapter()

        Log.d("Id Grabbed : ",dataUserShared.getInt("id",0).toString())

        binding.fabAdd.setOnClickListener(){
            val judul : EditText = dialog.findViewById(R.id.edtJudul)
            val catatan : EditText = dialog.findViewById(R.id.edtCatatan)
            val submit : Button = dialog.findViewById(R.id.btnSubmit)
            val attachImage : LinearLayout = dialog.findViewById(R.id.linearAttachFile)

            attachImage.setOnClickListener {
                Log.d("AttachImage Onclick", "Clicked: ")
                pickImageFromGallery()
                dialog.dismiss()
            }

            submit.setOnClickListener{
                when {
                    judul.text.toString().isEmpty() -> {
                        Toast.makeText(context, "Judul Masih Kosong", Toast.LENGTH_SHORT).show()
                    }
                    catatan.text.toString().isEmpty() -> {
                        Toast.makeText(context, "Catatan Masih Kosong", Toast.LENGTH_SHORT).show() }

                    else -> {
                        note.let { note ->
                            note?.title = judul.text.toString()
                            note?.description = catatan.text.toString()
                            note?.date = DateHelper.getCurrentDate()
                            note?.idUser = user!!.id
                        }
                        noteAddUpdateViewModel.insert(note as Note)
                        postUser(0,judul.text.toString(),catatan.text.toString(),DateHelper.getCurrentDate(),dataUserShared.getInt("id",0).toString())
                        Toast.makeText(context, "Berhasil menambahkan satu data", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        observer()
                    }
                }
            }
            dialog.show()
        }

        binding.tvLogout.setOnClickListener(){
            clearData()
            gotoLogin()
            Toast.makeText(context, "Logout Berhasil", Toast.LENGTH_SHORT).show()
        }

    }

    fun getData(){
        user.let { user ->
            user?.id = dataUserShared.getInt("id",0)
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
                    Toast.makeText(context, "${note.title} DELETED", Toast.LENGTH_SHORT).show()
                    observer()
                }

                override fun onUpdate(note: Note) {
                    noteAddUpdateViewModel.update(note)
                    Toast.makeText(context, "Notes updated", Toast.LENGTH_SHORT).show()
                    observer()
                }
            }
        )
        observer()
    }
    fun observer(){
        val mainViewModel = obtainViewModel(requireActivity())
        mainViewModel.getAllNotes(user?.id.toString()).observe(requireActivity(), { noteList ->
            if (noteList != null) {
                adapter.setListNotes(noteList)
                if(noteList.size == 0) {
                    binding.tvNoteEmpty.visibility = View.VISIBLE
                } else {
                    binding.tvNoteEmpty.visibility = View.INVISIBLE
                }
            }
        })

        binding?.rvNotes?.layoutManager = LinearLayoutManager(context)
        binding?.rvNotes?.setHasFixedSize(true)
        binding?.rvNotes?.adapter = adapter

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val dataDelete = adapter.listNotes[position]

                noteAddUpdateViewModel.delete(dataDelete)

                Snackbar.make(view!!,"Notes Deleted",Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO"){
                        noteAddUpdateViewModel.insert(dataDelete)
                    }
                    show()
                }

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvNotes)
    }
    private fun postUser(id: Int,title:String,description:String,date: String,userid: String) {
        val client = ApiConfig.getApiService().createNotes(NoteResponseItem(id,title,description,date, userid))
        client.enqueue(object : Callback<PostNotesResponse> {
            override fun onResponse(
                call: Call<PostNotesResponse>,
                response: Response<PostNotesResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PostNotesResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun pickImageFromGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            Log.d(TAG, "onActivityResult: filepath : ${filePath} ")
            selectedFile = filePath.toString()
            dialog.show()
        }
    }


    fun clearData(){
        var pref = dataUserShared.edit()
        pref.clear()
        pref.apply()
    }
    fun gotoLogin(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentHome_to_fragmentLogin)
    }
}