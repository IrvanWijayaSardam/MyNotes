package com.aminivan.mynotes.fragment

import KEY_IMAGE_URI
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.CustomDialogBinding
import com.aminivan.mynotes.databinding.FragmentSecretBinding
import com.aminivan.mynotes.helper.DateHelper
import com.aminivan.mynotes.helper.SwipeToDeleteCallback
import com.aminivan.mynotes.helper.SwipeToSeeCallBack
import com.aminivan.mynotes.helper.URIPathHelper
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.PostNotesResponse
import com.aminivan.mynotes.response.ResponseFetchAll
import com.aminivan.mynotes.response.UpdateNotesResponse
import com.aminivan.mynotes.viewmodel.*
import com.aminivan.mynotes.workers.BlurWorker
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class FragmentSecret : Fragment() {
    private var _binding : FragmentSecretBinding? = null
    private val binding get() = _binding!!
    lateinit var dialogBinding: CustomDialogBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    //    lateinit var dataUserShared : SharedPreferences
    lateinit var selectedFile : String
    lateinit var dialog : Dialog
    lateinit var dialogUpdate : Dialog
    lateinit var profile : Bitmap
    private val handler = Handler()
    lateinit var viewModeluser : UserViewModel

    private val pickImage = 100
    private val updateImage = 69
    lateinit var imageUri : Uri
    lateinit var defaultUri : String
    var imageUriDownload : Uri? = null
    lateinit var token : String
    var secret : Boolean = false
    private var note: Note? = null
    private var noteUpdate : Note? = null
    private var user : User? = null
    private lateinit var adapter: NoteSecretAdapter
    private val viewModel: BlurViewModel by viewModels { BlurViewModelFactory(activity) }
    val myExecutor = Executors.newSingleThreadExecutor()
    val myHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecretBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //onResumeHandler()
        //UpdateHandler()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDialog()
        var context = binding.rvNotesSecret.context
        secret = true
        selectedFile = "Attach File"
        imageUri = Uri.parse("DefaultUri")
        defaultUri = "Default"
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        note = Note()
        noteUpdate = Note()
        user = User()
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        Log.d(ContentValues.TAG, "onViewCreated: Sudah Masuk Home")
        token = ""
        viewModeluser = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModeluser.dataUser.observe(requireActivity(),{
            Log.d(ContentValues.TAG, "FragmentHome: ${it.id}")
            Log.d(ContentValues.TAG, "FragmentHome: ${it.name}")
            Log.d(ContentValues.TAG, "FragmentHome: ${it.email}")
            Log.d(ContentValues.TAG, "FragmentHome: ${it.password}")
            Log.d(ContentValues.TAG, "FragmentHome: ${it.jk}")
            Log.d(ContentValues.TAG, "FragmentHome: ${it.token}")


            user.let { user ->
                user!!.id = it.id
                user!!.name = it.name
                user!!.email = it.email
                user!!.password = it.password
                user!!.jk = it.jk
                user!!.profile = it.profile
            }
            token = it.token
            setAdapter()
        })

        binding.ivBack.setOnClickListener {
            gotoHome()
        }

    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun setAdapter(){
        adapter = NoteSecretAdapter(
            object : NoteSecretAdapter.OnAdapterListener {
                override fun onDelete(note: Note) {
                    noteAddUpdateViewModel.delete(note)
                    val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                    viewModel.deleteNote(token,note.id)
                    Toast.makeText(context, "${note.title} DELETED", Toast.LENGTH_SHORT).show()
                    observer()
                }
                override fun onUpdate(note: Note) {
                    noteUpdate.let { noteUpdate ->
                        noteUpdate!!.id = note.id
                        noteUpdate!!.title = note.title
                        noteUpdate!!.description = note.description
                        noteUpdate!!.date = note.date
                        noteUpdate!!.idUser = note.idUser
                        noteUpdate!!.image = note.image
                        noteUpdate!!.secret = note.secret
                    }

                    Log.d(ContentValues.TAG, "onUpdate: noteUpdate title ${noteUpdate!!.id}")
                    var dialogUpdate = Dialog(requireContext())
                    dialogUpdate.setContentView(R.layout.custom_dialog_update);
                    var tvTitleUpdate : EditText = dialogUpdate.findViewById(R.id.edtJudulUpdate)
                    var tvDeskripsi : EditText = dialogUpdate.findViewById(R.id.edtCatatanUpdate)
                    var btnSubmit : Button = dialogUpdate.findViewById(R.id.btnSubmitUpdate)
                    var linearUpdate : LinearLayout = dialogUpdate.findViewById(R.id.linearAttachFileUpdate)
                    val ivLockUpdate : ImageView = dialogUpdate.findViewById(R.id.ivLockUpdate)
                    val ivUnlockUpdate : ImageView = dialogUpdate.findViewById(R.id.ivUnlockUpdate)


                    ivUnlockUpdate.visibility = View.VISIBLE
                    ivLockUpdate.setOnClickListener {
                        secret = true
                        ivLockUpdate.visibility = View.INVISIBLE
                        ivUnlockUpdate.visibility = View.VISIBLE
                        Toast.makeText(context, "This Notes Will Be Added As Secret Notes", Toast.LENGTH_SHORT).show()
                        Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
                    }

                    ivUnlockUpdate.setOnClickListener {
                        secret = false
                        ivLockUpdate.visibility = View.VISIBLE
                        ivUnlockUpdate.visibility = View.INVISIBLE
                        Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
                    }


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
                            note?.secret = secret
                        }
                        if (selectedFile.equals("Attach File")){
                            noteAddUpdateViewModel.update(note!!)
                            val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                            viewModel.updateNote(token.toString(),note!!.id,tvTitleUpdate.text.toString(),tvDeskripsi.text.toString(), note!!.date.toString(),user!!.id ,note!!.image.toString())
                            setAdapter()
                            dialogUpdate.dismiss()
                        } else {
                            noteAddUpdateViewModel.update(note!!)
                            val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                            viewModel.updateNote(token.toString(),note!!.id,tvTitleUpdate.text.toString(),tvDeskripsi.text.toString(), note!!.date.toString(),user!!.id, imageUri.toString())
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
        mainViewModel.getAllNotes(user!!.id.toString(),true).observe(requireActivity(), { noteList ->
            if (noteList != null) {
                adapter.setListNotes(noteList)
                if(noteList.size == 0) {
                    binding.tvNoteEmpty.visibility = View.VISIBLE
                    Log.d(ContentValues.TAG, "observer: $noteList")
                } else {
                    binding.tvNoteEmpty.visibility = View.INVISIBLE
                    Log.d(ContentValues.TAG, "observerEmpty: $noteList")
                }
            }
        })

        binding?.rvNotesSecret?.layoutManager = LinearLayoutManager(context)
        binding?.rvNotesSecret?.setHasFixedSize(true)
        binding?.rvNotesSecret?.adapter = adapter


        val swipeToDeleteCallback = object : SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val dataDelete = adapter.listNotes[position]
                noteAddUpdateViewModel.delete(dataDelete)
                val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                viewModel.deleteNote(token,dataDelete.id)
                Snackbar.make(view!!,"Notes Deleted",Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO"){
                        val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                        viewModel.postNotes(token.toString(),dataDelete.id,dataDelete.title.toString(),dataDelete.description.toString(),dataDelete.date.toString(),user!!.id,dataDelete.image.toString())
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
                imageUri = Uri.parse(adapter.listNotes[position].image.toString())

                Log.d(TAG, "onSwiped: imageUri ${imageUri}")
                observer()

                var mImage: Bitmap?
                myExecutor.execute {
                    mImage = mLoad(imageUri.toString())
                    myHandler.post {
                        if(mImage!=null){
                            mSaveMediaToStorage(imageUri.toString(),mImage)
                        }
                    }
                }

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        val itemTouchHelperSee = ItemTouchHelper(swipeToSeeCallBack)
        itemTouchHelper.attachToRecyclerView(binding.rvNotesSecret)
        itemTouchHelperSee.attachToRecyclerView(binding.rvNotesSecret)
    }

    fun setDialog(){
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogUpdate = Dialog(requireContext())
        dialogUpdate.setContentView(R.layout.custom_dialog_update);
        dialogUpdate.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


    }

    private fun pickImageFromGallery(code : Int) {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 69) {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
            uploadToFirebase()
            Log.d(ContentValues.TAG, "onActivityResult: filepath : ${filePath} ")
            Log.d(ContentValues.TAG, "GetImageUriDefault: ${imageUri.toString()}")
            selectedFile = filePath.toString()
            Log.d(ContentValues.TAG, "onActivityResult bitmap: ${profile}")
            onResumeUpdateHandler()
            dialogUpdate.show()
        } else {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
            uploadToFirebase()
            Log.d(ContentValues.TAG, "onActivityResult: filepath : ${filePath} ")
            Log.d(ContentValues.TAG, "GetImageUriDefault: ${imageUri.toString()}")
            selectedFile = filePath.toString()
            Log.d(ContentValues.TAG, "onActivityResult bitmap: ${profile}")
            Log.d(ContentValues.TAG, "onActivityResult: LEWAT ON ACTIVITY RESULT INSERT")
            dialog.show()
        }
    }

    fun uploadToFirebase(){
        val formatter = SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

        storageReference.putFile(imageUri).addOnSuccessListener {
            Log.d(ContentValues.TAG, "uploadToFirebase: SUCCESS")
            it.storage.downloadUrl.addOnCompleteListener {
                defaultUri = it.result.toString()
                Log.d("Get Download URL", "Get Download URL : ${it.result.toString()}")
                imageUri = it.result
            }.addOnFailureListener {
                Log.d(ContentValues.TAG, "On Failure :${it.message.toString()} ")
            }
        }.addOnFailureListener{
            Log.d(ContentValues.TAG, "uploadToFirebase: YOU'RE SUCH A FAILURE")
        }
    }

    fun gotoHome(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentSecret_to_fragmentHome)
    }

    fun onResumeUpdateHandler(){
        dialogUpdate.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        val tvAttachImage : TextView = dialogUpdate.findViewById(R.id.tvAttachFileUpdate)
        val icCancel : ImageView = dialogUpdate.findViewById(R.id.ivCancelUpdate)
        val progressBar : ProgressBar = dialogUpdate.findViewById(R.id.progressBar)
        val btnSubmit : Button = dialogUpdate.findViewById(R.id.btnSubmitUpdate)
        val edtTitleUpdate : EditText = dialogUpdate.findViewById(R.id.edtJudulUpdate)
        val edtDescription : EditText = dialogUpdate.findViewById(R.id.edtCatatanUpdate)
        val ivLockUpdate : ImageView = dialogUpdate.findViewById(R.id.ivLockUpdate)
        val ivUnlockUpdate : ImageView = dialogUpdate.findViewById(R.id.ivUnlockUpdate)


        ivUnlockUpdate.visibility = View.VISIBLE
        ivLockUpdate.setOnClickListener {
            secret = true
            ivLockUpdate.visibility = View.INVISIBLE
            ivUnlockUpdate.visibility = View.VISIBLE
            Toast.makeText(context, "This Notes Will Be Added As Secret Notes", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
        }

        ivUnlockUpdate.setOnClickListener {
            secret = false
            ivLockUpdate.visibility = View.VISIBLE
            ivUnlockUpdate.visibility = View.INVISIBLE
            Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
        }

        edtTitleUpdate.setText(noteUpdate!!.title)
        edtDescription.setText(noteUpdate!!.description)
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
                noteUpdate.let { noteUpdate ->
                    noteUpdate?.title = edtTitleUpdate.text.toString()
                    noteUpdate?.description = edtDescription.text.toString()
                    noteUpdate?.date = noteUpdate!!.date
                    noteUpdate?.idUser = noteUpdate!!.idUser
                    noteUpdate?.image = noteUpdate!!.image
                    noteUpdate?.secret = secret
                }
                noteAddUpdateViewModel.update(Note(0,edtTitleUpdate.text.toString(),edtDescription.text.toString(),noteUpdate!!.date.toString(),noteUpdate!!.idUser,noteUpdate!!.image.toString(),noteUpdate!!.secret))
                val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                viewModel.updateNote(token.toString(),noteUpdate!!.id,edtTitleUpdate.text.toString(),edtDescription.text.toString(), noteUpdate!!.date.toString(),user!!.id, noteUpdate!!.image.toString())
                Log.d(ContentValues.TAG, "onResumeUpdateHandler: Masuk Btn Submit OnUpdateResume if Attach File")
                Log.d(ContentValues.TAG, "onResumeUpdateHandler: Note ${noteUpdate!!.title.toString()}")
                setAdapter()
                dialogUpdate.dismiss()
            } else {
                noteAddUpdateViewModel.update(Note(note!!.id,edtTitleUpdate.text.toString(),edtDescription.text.toString(),note!!.date.toString(),note!!.idUser,imageUri.toString(),secret))
                val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                viewModel.updateNote(token,noteUpdate!!.id,edtTitleUpdate.text.toString(),edtDescription.text.toString(), noteUpdate!!.date.toString(),user!!.id, imageUri.toString())
                Log.d(ContentValues.TAG, "onResumeUpdateHandler: Masuk Btn Submit OnUpdateResume if With Image")
                Log.d(ContentValues.TAG, "onResumeUpdateHandler: ${noteUpdate!!.title} uri ${noteUpdate!!.image}")
                setAdapter()
                dialogUpdate.dismiss()
            }
        }
    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "mLoad: Error")
        }
        return null
    }

    // Function to convert string to URL
    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }
    // Function to save image on the device.
    // Refer: https://www.geeksforgeeks.org/circular-crop-an-image-and-save-it-to-the-file-in-android/
    private fun mSaveMediaToStorage(filename : String,bitmap: Bitmap?) {
        val filename = "${filename}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireContext().contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                imageUriDownload = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                Log.d(TAG, "mSaveMediaToStorage: imageUriDownload ${imageUriDownload}")
                viewModel.setImageUri(imageUriDownload)
                viewModel.applyBlur(3)
                dialog = Dialog(requireContext())
                dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setContentView(R.layout.custom_dialog_attachment);
                viewModel.workInfo.observe(viewLifecycleOwner, {
                    val workInfo = it[0]
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val data = workInfo.outputData
                        val uri = data.getString(KEY_IMAGE_URI)
                        Log.d(TAG, "mSaveMediaToStorage: uriGetted ${uri}")
                        Glide.with(requireContext()).load(uri).into(dialog.findViewById(R.id.imageDialogue))
                        //Log.d(TAG, "mSaveMediaToStorage: Blur Worker outputUri ")
                    }
                })
                fos = imageUriDownload?.let { resolver.openOutputStream(it) }
                dialog.show()
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            Log.d(TAG, "mSaveMediaToStorage: ${imagesDir}")
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context , "Saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }
}