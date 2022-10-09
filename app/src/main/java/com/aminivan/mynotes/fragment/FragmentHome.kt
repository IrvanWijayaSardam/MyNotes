package com.aminivan.mynotes.fragment

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
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
import com.aminivan.mynotes.helper.SwipeToSeeCallBack
import com.aminivan.mynotes.helper.URIPathHelper
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.PostNotesResponse
import com.aminivan.mynotes.response.ResponseFetchAll
import com.aminivan.mynotes.response.UpdateNotesResponse
import com.aminivan.mynotes.viewmodel.NoteAdapter
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class FragmentHome : Fragment() {

    lateinit var binding : FragmentHomeBinding
    lateinit var dialogBinding: CustomDialogBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var dataUserShared : SharedPreferences
    lateinit var selectedFile : String
    lateinit var dialog : Dialog
    lateinit var dialogUpdate : Dialog
    lateinit var profile : Bitmap
    private val handler = Handler()


    private val pickImage = 100
    private val updateImage = 69
    lateinit var imageUri : Uri
    lateinit var defaultUri : String

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
        onResumeHandler()
        onResumeUpdateHandler()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDialog()
        var context = binding.rvNotes.context
        selectedFile = "Attach File"
        imageUri = Uri.parse("DefaultUri")
        defaultUri = "Default"
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        note = Note()
        user = User()
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)
        getData()
        setAdapter()

        Log.d("Id Grabbed : ",dataUserShared.getInt("id",0).toString())

        binding.tvWelcomeHome.setOnClickListener {
            gotoProfile()
        }

        binding.fabAdd.setOnClickListener(){
            val judul : EditText = dialog.findViewById(R.id.edtJudul)
            val catatan : EditText = dialog.findViewById(R.id.edtCatatan)
            val submit : Button = dialog.findViewById(R.id.btnSubmit)
            val attachImage : LinearLayout = dialog.findViewById(R.id.linearAttachFile)
            val icCancel : ImageView = dialog.findViewById(R.id.ivCancel)
            val tvAttachImage : TextView = dialog.findViewById(R.id.tvAttachFile)

            icCancel.setOnClickListener {
                selectedFile = "Attach File"
                tvAttachImage.text = selectedFile
                icCancel.visibility = View.INVISIBLE
                deleteImage(defaultUri)
                defaultUri = "Default"
            }

            attachImage.setOnClickListener {
                Log.d("AttachImage Onclick", "Clicked: ")
                pickImageFromGallery(pickImage)
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
                            note?.image = defaultUri
                        }
                        if(defaultUri.equals("Default")) {
                            postNotes(dataUserShared.getString("token","").toString(),0,note?.title.toString(),note?.description.toString(),DateHelper.getCurrentDate(),"Default")
                            noteAddUpdateViewModel.insert(note!!)
                            Thread.sleep(100)
                            setAdapter()
                            judul.text.clear()
                            catatan.text.clear()
                            defaultUri = "Default"
                        } else {
                            postNotes(dataUserShared.getString("token","").toString(),0,note?.title.toString(),note?.description.toString(),DateHelper.getCurrentDate(),defaultUri)
                            noteAddUpdateViewModel.insert(note!!)
                            Thread.sleep(100)
                            setAdapter()
                            judul.text.clear()
                            catatan.text.clear()
                            defaultUri = "Default"
                        }
                        setAdapter()
                        Toast.makeText(context, "Berhasil menambahkan satu data", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
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
            user?.name = dataUserShared.getString("username","")
            user?.email = dataUserShared.getString("email","")
            user?.password = dataUserShared.getString("password","")
            user?.profile = dataUserShared.getString("profile","")
        }

        binding.tvWelcomeHome.setText("${user?.name} !")
    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun setAdapter(){
        retriveNotes(dataUserShared.getString("token","").toString())
        adapter = NoteAdapter(
            object : NoteAdapter.OnAdapterListener {
                override fun onDelete(note: Note) {
                    noteAddUpdateViewModel.delete(note)
                    deleteNote(dataUserShared.getString("token","").toString(),note.id)
                    Toast.makeText(context, "${note.title} DELETED", Toast.LENGTH_SHORT).show()
                    observer()
                }
                override fun onUpdate(note: Note) {
                    note.let { note ->
                        note?.title = note.title
                        note?.description = note.description
                        note?.date = note.date
                        note?.idUser = note.idUser
                        note?.image = note.image
                    }
                    var dialogUpdate = Dialog(requireContext())
                    dialogUpdate.setContentView(R.layout.custom_dialog_update);
                    var tvTitleUpdate : EditText = dialogUpdate.findViewById(R.id.edtJudulUpdate)
                    var tvDeskripsi : EditText = dialogUpdate.findViewById(R.id.edtCatatanUpdate)
                    var btnSubmit : Button = dialogUpdate.findViewById(R.id.btnSubmitUpdate)
                    var linearUpdate : LinearLayout = dialogUpdate.findViewById(R.id.linearAttachFileUpdate)

                    tvTitleUpdate.setText(note.title)
                    tvDeskripsi.setText(note.description)

                    dialogUpdate.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialogUpdate.show()

                    btnSubmit.setOnClickListener {
                        note.let { note ->
                            note?.title = tvTitleUpdate.text.toString()
                            note?.description = tvDeskripsi.text.toString()
                            note?.date = note.date
                            note?.idUser = note.idUser
                            note?.image = note.image
                        }
                        if (selectedFile.equals("Attach File")){
                            noteAddUpdateViewModel.update(note!!)
                            updateNote(dataUserShared.getString("token","").toString(),note!!.id,
                                tvTitleUpdate.text.toString(),
                                tvDeskripsi.text.toString(), note!!.date.toString(), note!!.image.toString()
                            )
                            try {
                                Thread.sleep(3000)
                            } catch (e : InterruptedException){
                                e.printStackTrace()
                            }
                            setAdapter()
                            dialogUpdate.dismiss()
                        } else {
                            noteAddUpdateViewModel.update(note!!)
                            updateNote(dataUserShared.getString("token","").toString(),note!!.id,
                                tvTitleUpdate.text.toString(),
                                tvDeskripsi.text.toString(), note!!.date.toString(), imageUri.toString()
                            )
                            try {
                                Thread.sleep(3000)
                            } catch (e : InterruptedException){
                                e.printStackTrace()
                            }
                            setAdapter()
                            dialogUpdate.dismiss()
                        }
                    }

                    linearUpdate.setOnClickListener{
                        pickImageFromGallery(updateImage)
                        dialogUpdate.dismiss()
                    }

                    observer()
                }
            }
        )
        observer()
    }
    fun observer(){
        val mainViewModel = obtainViewModel(requireActivity())
        mainViewModel.getAllNotes(dataUserShared.getInt("id",0).toString()).observe(requireActivity(), { noteList ->
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
                deleteNote(dataUserShared.getString("token","").toString(),dataDelete.id)
                Snackbar.make(view!!,"Notes Deleted",Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO"){
                        postNotes(dataUserShared.getString("token","").toString(),dataDelete.id,dataDelete.title.toString(),dataDelete.description.toString(),dataDelete.date.toString(),dataDelete.image.toString())
                        noteAddUpdateViewModel.insert(dataDelete)
                    }
                    show()
                }

            }
        }
        val swipeToSeeCallBack = object : SwipeToSeeCallBack(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.custom_dialog_attachment);
                val ivAttachment : ImageView = dialog.findViewById(R.id.imageDialogue)

                Glide.with(requireContext()).load(adapter.listNotes[position].image.toString()).into(ivAttachment)

                dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show()
                observer()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        val itemTouchHelperSee = ItemTouchHelper(swipeToSeeCallBack)
        itemTouchHelper.attachToRecyclerView(binding.rvNotes)
        itemTouchHelperSee.attachToRecyclerView(binding.rvNotes)
    }

    fun setDialog(){
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogUpdate = Dialog(requireContext())
        dialogUpdate.setContentView(R.layout.custom_dialog_update);
        dialogUpdate.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    fun deleteImage(downloadLink : String){
        val mFirebaseStorage = FirebaseStorage.getInstance().getReference().getStorage();
        val photoRef = mFirebaseStorage.getReferenceFromUrl(downloadLink)
        photoRef.delete().addOnSuccessListener {
            Log.d(TAG, "deleteImage: Image Deleted")
        }.addOnFailureListener {
            Log.d(TAG, "deleteImage: Failed deleting image "+it.message)
        }

    }

    private fun postNotes(token : String,id: Int,title:String,description:String,date: String, image : String) {
        val client = ApiConfig.getApiService().createNotes(token,NoteResponseItem(id,title,description,date, dataUserShared.getInt("id",0),image))
        client.enqueue(object : Callback<PostNotesResponse> {
            override fun onResponse(
                call: Call<PostNotesResponse>,
                response: Response<PostNotesResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Toast.makeText(context, "Notes updated", Toast.LENGTH_SHORT).show()
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                    Log.d(TAG, "onResponse: ${token}")
                    Log.d(TAG, "onResponse: ${id}")
                    Log.d(TAG, "onResponse: ${title}")
                    Log.d(TAG, "onResponse: ${description}")
                    Log.d(TAG, "onResponse: ${date}")
                    Log.d(TAG, "onResponse: ${image}")
                }
            }

            override fun onFailure(call: Call<PostNotesResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun updateNote(token: String,id: Int,title: String,description: String,date: String,image: String){
        val client = ApiConfig.getApiService().updateNotes(token,id.toString(),
            NoteResponseItem(id, title, description, date, dataUserShared.getInt("id",0), image))
        client.enqueue(object : Callback<UpdateNotesResponse> {
            override fun onResponse(
                call: Call<UpdateNotesResponse>,
                response: Response<UpdateNotesResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UpdateNotesResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun deleteNote(token: String,id: Int) {
        val client = ApiConfig.getApiService().deleteNotes(token,id.toString())
        client.enqueue(object : Callback<ResponseFetchAll> {
            override fun onResponse(
                call: Call<ResponseFetchAll>,
                response: Response<ResponseFetchAll>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseFetchAll>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun pickImageFromGallery(code : Int) {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
            uploadToFirebase()
            Log.d(TAG, "onActivityResult: filepath : ${filePath} ")
            Log.d(TAG, "GetImageUriDefault: ${imageUri.toString()}")
            selectedFile = filePath.toString()
            Log.d(TAG, "onActivityResult bitmap: ${profile}")
            dialog.show()
        } else if (resultCode == RESULT_OK && requestCode == updateImage) {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
            uploadToFirebase()
            Log.d(TAG, "onActivityResult: filepath : ${filePath} ")
            Log.d(TAG, "GetImageUriDefault: ${imageUri.toString()}")
            selectedFile = filePath.toString()
            Log.d(TAG, "onActivityResult bitmap: ${profile}")
            dialogUpdate.show()
        }
    }

    fun uploadToFirebase(){
        val formatter = SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

        storageReference.putFile(imageUri).addOnSuccessListener {
            Log.d(TAG, "uploadToFirebase: SUCCESS")
            it.storage.downloadUrl.addOnCompleteListener {
                defaultUri = it.result.toString()
                Log.d("Get Download URL", "Get Download URL : ${it.result.toString()}")
                imageUri = it.result
            }.addOnFailureListener {
                Log.d(TAG, "On Failure :${it.message.toString()} ")
            }
        }.addOnFailureListener{
            Log.d(TAG, "uploadToFirebase: YOU'RE SUCH A FAILURE")
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

    fun gotoProfile(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentHome_to_fragmentProfile)
    }

    private fun retriveNotes(token : String) {
        val client = ApiConfig.getApiService().getNotes(token)
        client.enqueue(object : Callback<ResponseFetchAll> {
            override fun onResponse(
                call: Call<ResponseFetchAll>,
                response: Response<ResponseFetchAll>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!.data!!.notes
                    if (responseBody != null) {
                        Log.d(TAG, "onResponse: ${responseBody}")
                        for (i in 0 until responseBody.size) {
                            note.let { note ->
                                note?.id = responseBody[i]!!.id!!.toInt()
                                note?.title = responseBody[i]!!.title
                                note?.description = responseBody[i]!!.description
                                note?.date = responseBody[i]!!.date
                                note?.idUser = responseBody[i]!!.user!!.id!!.toInt()
                                note?.image = responseBody[i]!!.image
                                noteAddUpdateViewModel.insert(Note(responseBody[i]!!.id!!.toInt(),responseBody[i]!!.title,responseBody[i]!!.description,
                                    responseBody[i]!!.date,responseBody[i]!!.user!!.id!!.toInt(),responseBody[i]!!.image))
                            }
//                            Log.d(TAG, "onResponse: ${responseBody.size.toString()}")
//                            Log.d(TAG, "onResponse: ${responseBody[i]!!.description}")
                        }
                    }
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<ResponseFetchAll>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun onResumeHandler(){
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog);

        val tvAttachImage : TextView = dialog.findViewById(R.id.tvAttachFile)
        val icCancel : ImageView = dialog.findViewById(R.id.ivCancel)
        val progressBar : ProgressBar = dialog.findViewById(R.id.progressBar)
        val btnSubmit : Button = dialog.findViewById(R.id.btnSubmit)
        tvAttachImage.text = selectedFile
        if (selectedFile.length >30) {
            icCancel.visibility = View.VISIBLE
        } else {
            icCancel.visibility = View.INVISIBLE
        }

        progressBar.visibility = View.VISIBLE

        var i = progressBar.progress

        Thread(Runnable {
            // this loop will run until the value of i becomes 99
            while (i < 100) {
                i += 1
                // Update the progress bar and display the current value
                handler.post(Runnable {
                    progressBar.progress = i
                    btnSubmit.isClickable = false
                })
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            progressBar.visibility = View.INVISIBLE
            btnSubmit.isClickable = true

        }).start()
    }
    fun onResumeUpdateHandler(){
        val tvAttachImage : TextView = dialogUpdate.findViewById(R.id.tvAttachFileUpdate)
        val icCancel : ImageView = dialogUpdate.findViewById(R.id.ivCancelUpdate)
        val progressBar : ProgressBar = dialogUpdate.findViewById(R.id.progressBar)
        val btnSubmit : Button = dialogUpdate.findViewById(R.id.btnSubmitUpdate)
        val edtTitleUpdate : EditText = dialogUpdate.findViewById(R.id.edtJudulUpdate)
        val edtDescription : EditText = dialogUpdate.findViewById(R.id.edtCatatanUpdate)

        edtTitleUpdate.setText(note!!.title)
        edtDescription.setText(note!!.description)
        tvAttachImage.text = selectedFile
        if (selectedFile.length >30) {
            icCancel.visibility = View.VISIBLE
        } else {
            icCancel.visibility = View.INVISIBLE
        }

        progressBar.visibility = View.VISIBLE

        var i = progressBar.progress

        Thread(Runnable {
            // this loop will run until the value of i becomes 99
            while (i < 100) {
                i += 1
                // Update the progress bar and display the current value
                handler.post(Runnable {
                    progressBar.progress = i
                    btnSubmit.isClickable = false
                })
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            progressBar.visibility = View.INVISIBLE
            btnSubmit.isClickable = true

        }).start()

        btnSubmit.setOnClickListener {
            if (selectedFile.equals("Attach File")){
                note.let { note ->
                    note?.title = edtTitleUpdate.text.toString()
                    note?.description = edtDescription.text.toString()
                    note?.date = note!!.date
                    note?.idUser = note!!.idUser
                    note?.image = note!!.image
                }
                noteAddUpdateViewModel.update(note!!)
                updateNote(dataUserShared.getString("token","").toString(),note!!.id,
                    edtTitleUpdate.text.toString(),
                    edtDescription.text.toString(), note!!.date.toString(), note!!.image.toString()
                )

                setAdapter()
                dialogUpdate.dismiss()
            } else {
                note.let { note ->
                note?.title = edtTitleUpdate.text.toString()
                note?.description = edtDescription.text.toString()
                note?.date = note!!.date
                note?.idUser = note!!.idUser
                note?.image = imageUri.toString()
                }
                noteAddUpdateViewModel.update(note!!)
                updateNote(dataUserShared.getString("token","").toString(),note!!.id,
                    edtTitleUpdate.text.toString(),
                    edtDescription.text.toString(), note!!.date.toString(), imageUri.toString()
                )

                setAdapter()
                dialogUpdate.dismiss()
            }
        }
    }
}