package com.aminivan.mynotes.fragment

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
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
import com.aminivan.mynotes.viewmodel.*
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

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var dialogBinding: CustomDialogBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var selectedFile : String
    lateinit var dialog : Dialog
    lateinit var dialogUpdate : Dialog
    lateinit var profile : Bitmap
    private val handler = Handler()
    lateinit var viewModeluser : UserViewModel


    private val updateImage = 69
    lateinit var imageUri : Uri
    lateinit var defaultUri : String
    lateinit var token : String
    var secret : Boolean = false

    private var note: Note? = null
    private var noteUpdate : Note? = null
    private var user : User? = null
    var idUser : Int = 0

    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
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
        noteUpdate = Note()
        user = User()
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)

        val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
        viewModel.getLiveDataUsers().observe(viewLifecycleOwner,{
            Log.d(TAG, "onViewCreated: Observe${it}")
            if(it == null) {
                Log.d(TAG, "onViewCreated: Data Kosong ${it}")
                Toast.makeText(context, "Username / Password salah", Toast.LENGTH_SHORT).show()
            } else {
                idUser = it.data?.id!!.toInt()
                user!!.id = it.data?.id!!.toInt()
                user!!.email = it.data?.email
                user!!.name = it.data?.name
                user!!.profile = it.data?.profile
                user!!.jk = it.data?.jk
                Log.d(TAG, "onViewCreated: token after login ${it.data.token}")
                retrieveNotes(it.data.token.toString())
                viewModeluser.editData(user!!.id, user!!.name.toString(),user!!.email.toString(),"",user!!.profile.toString(),it.data?.jk.toString(),it.data.token.toString())
            }
        })

        Log.d(TAG, "onViewCreated: Sudah Masuk Home")
        token = ""
        viewModeluser = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModeluser.dataUser.observe(requireActivity(),{
            Log.d(TAG, "FragmentHome: ${it.id}")
            Log.d(TAG, "FragmentHome: ${it.name}")
            Log.d(TAG, "FragmentHome: ${it.email}")
            Log.d(TAG, "FragmentHome: ${it.password}")
            Log.d(TAG, "FragmentHome: ${it.jk}")
            Log.d(TAG, "FragmentHome: ${it.token}")

            binding.tvWelcomeHome.setText(it.name)

            user.let { user ->
                user!!.id = it.id
                user!!.name = it.name
                user!!.email = it.email
                user!!.password = it.password
                user!!.jk = it.jk
                user!!.profile = it.profile
            }
            token = it.token
            Glide.with(this)
                .load(it.profile)
                .error(R.drawable.account)
                .circleCrop()
                .into(binding.ivMan)
            setAdapter()
        })

        binding.tvWelcomeHome.setOnClickListener {
            gotoProfile()
        }

        binding.ivMan.setOnClickListener{
            gotoProfile()
        }

        binding.ivSecret.setOnClickListener{
            gotoSecret()
        }

        binding.fabAdd.setOnClickListener(){
            setDialog()
            val judul : EditText = dialog.findViewById(R.id.edtJudul)
            val catatan : EditText = dialog.findViewById(R.id.edtCatatan)
            val submit : Button = dialog.findViewById(R.id.btnSubmit)
            val attachImage : LinearLayout = dialog.findViewById(R.id.linearAttachFile)
            val icCancel : ImageView = dialog.findViewById(R.id.ivCancel)
            val tvAttachImage : TextView = dialog.findViewById(R.id.tvAttachFile)
            val ivLock : ImageView = dialog.findViewById(R.id.ivLock)
            val ivUnlock : ImageView = dialog.findViewById(R.id.ivUnlock)

            ivLock.visibility = View.VISIBLE

            ivLock.setOnClickListener {
                secret = true
                ivLock.visibility = View.INVISIBLE
                ivUnlock.visibility = View.VISIBLE
                Toast.makeText(context, "This Notes Will Be Added As Secret Notes", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
            }

            ivUnlock.setOnClickListener {
                secret = false
                ivLock.visibility = View.VISIBLE
                ivUnlock.visibility = View.INVISIBLE
                Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
            }


            icCancel.setOnClickListener {
                selectedFile = "Attach File"
                tvAttachImage.text = selectedFile
                icCancel.visibility = View.INVISIBLE
                deleteImage(defaultUri)
                defaultUri = "Default"
            }

            attachImage.setOnClickListener {
                Log.d("AttachImage Onclick", "Clicked: ")
                pickImageFromGallery(100)
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
                            note?.secret = secret
                        }
                        if(defaultUri.equals("Default")) {
                            val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                            viewModel.postNotes(token,0,note?.title.toString(),note?.description.toString(),DateHelper.getCurrentDate(),user!!.id,"Default")
                            judul.text.clear()
                            catatan.text.clear()
                            defaultUri = "Default"
                            dialog.dismiss()
                        } else {
                            val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                            viewModel.postNotes(token.toString(),0,note?.title.toString(),note?.description.toString(),DateHelper.getCurrentDate(),user!!.id,defaultUri)
                            judul.text.clear()
                            catatan.text.clear()
                            defaultUri = "Default"
                            dialog.dismiss()
                        }
                        Toast.makeText(context, "Secret Status : ${note?.secret}", Toast.LENGTH_SHORT).show()
                        noteAddUpdateViewModel.insert(Note(0,note?.title.toString(),note?.description.toString(),note?.date.toString(),note?.idUser!!.toInt(),imageUri.toString(),note?.secret))
                        setAdapter()
                        Log.d(TAG, "onViewCreated: Berhasil Menambahkan ${note!!.description}${note!!.secret}")
                        dialog.dismiss()
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

    fun retrieveNotes(token : String) {
        val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
        viewModel.retriveNotes(token)
        viewModel.getLiveDataNote().observe(viewLifecycleOwner, {
            for(i in 0 until it?.data!!.notes!!.size){
                note.let { note ->
                    note?.id = it.data.notes!![i]!!.id!!.toInt()
                    note?.title = it.data.notes!![i]!!.title
                    note?.description = it.data.notes!![i]!!.description
                    note?.date = it.data.notes!![i]!!.date
                    note?.idUser = it.data.notes!![i]!!.user!!.id!!.toInt()
                    note?.image = it.data.notes!![i]!!.image
                    noteAddUpdateViewModel.insert(Note(it.data.notes!![i]!!.id!!.toInt(),it.data.notes!![i]!!.title,it.data.notes!![i]!!.description,
                        it.data.notes!![i]!!.date,it.data.notes!![i]!!.user!!.id!!.toInt(),it.data.notes!![i]!!.image,false))
                }
                Log.d(TAG, "retrieveNotes: retrieveNotesExecuted")
            }
        })
    }

    fun setAdapter(){
        adapter = NoteAdapter(
            object : NoteAdapter.OnAdapterListener {
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
                    Log.d(TAG, "onUpdate: onUpdate ${noteUpdate!!.id}")
                    var dialogUpdate = Dialog(requireContext())
                    dialogUpdate.setContentView(R.layout.custom_dialog_update);
                    var tvTitleUpdate : EditText = dialogUpdate.findViewById(R.id.edtJudulUpdate)
                    var tvDeskripsi : EditText = dialogUpdate.findViewById(R.id.edtCatatanUpdate)
                    var btnSubmit : Button = dialogUpdate.findViewById(R.id.btnSubmitUpdate)
                    var linearUpdate : LinearLayout = dialogUpdate.findViewById(R.id.linearAttachFileUpdate)
                    val ivLockUpdate : ImageView = dialogUpdate.findViewById(R.id.ivLockUpdate)
                    val ivUnlockUpdate : ImageView = dialogUpdate.findViewById(R.id.ivUnlockUpdate)


                    ivLockUpdate.visibility = View.VISIBLE
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
                            viewModel.updateNote(token.toString(),note!!.id,tvTitleUpdate.text.toString(),tvDeskripsi.text.toString(), note!!.date.toString(),user!!.id, note!!.image.toString())
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
        mainViewModel.getAllNotes(user!!.id.toString(),false).observe(requireActivity(), { noteList ->
            if (noteList != null) {
                adapter.setListNotes(noteList)
                if(noteList.size == 0) {
                    binding.tvNoteEmpty.visibility = View.VISIBLE
                    Log.d(TAG, "observer: $noteList")
                } else {
                    binding.tvNoteEmpty.visibility = View.INVISIBLE
                    Log.d(TAG, "observerEmpty: $noteList")
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

    private fun pickImageFromGallery(code : Int) {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
       if (resultCode == RESULT_OK && requestCode == 69) {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
            uploadToFirebase()
            Log.d(TAG, "onActivityResult: filepath : ${filePath} ")
            Log.d(TAG, "GetImageUriDefault: ${imageUri.toString()}")
            selectedFile = filePath.toString()
            Log.d(TAG, "onActivityResult bitmap: ${profile}")
            onResumeUpdateHandler()
            dialogUpdate.show()
        } else {
           imageUri = data?.data!!
           val uriPathHelper = URIPathHelper()
           val filePath = uriPathHelper.getPath(requireContext(), imageUri)
           profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
           uploadToFirebase()
           Log.d(TAG, "onActivityResult: filepath : ${filePath} ")
           Log.d(TAG, "GetImageUriDefault: ${imageUri.toString()}")
           selectedFile = filePath.toString()
           Log.d(TAG, "onActivityResult bitmap: ${profile}")
           Log.d(TAG, "onActivityResult: LEWAT ON ACTIVITY RESULT INSERT")
           onResumeHandler()
           dialog.show()
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

    fun gotoProfile(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentHome_to_fragmentProfile)
    }

    fun gotoSecret(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentHome_to_fragmentSecret)
    }


    fun onResumeHandler(){
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog);
        val tvAttachImage : TextView = dialog.findViewById(R.id.tvAttachFile)
        val edtJudul : EditText = dialog.findViewById(R.id.edtJudul)
        val edtDescription : EditText = dialog.findViewById(R.id.edtCatatan)
        val icCancel : ImageView = dialog.findViewById(R.id.ivCancel)
        val progressBar : ProgressBar = dialog.findViewById(R.id.progressBar)
        val btnSubmit : Button = dialog.findViewById(R.id.btnSubmit)
        val ivLock : ImageView = dialog.findViewById(R.id.ivLock)
        val ivUnlock : ImageView = dialog.findViewById(R.id.ivUnlock)

        dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show()

        tvAttachImage.text = selectedFile
        if (selectedFile.length >30) {
            icCancel.visibility = View.VISIBLE
        } else {
            icCancel.visibility = View.INVISIBLE
        }

        ivLock.visibility = View.VISIBLE
        ivLock.setOnClickListener {
            secret = true
            ivLock.visibility = View.INVISIBLE
            ivUnlock.visibility = View.VISIBLE
            Toast.makeText(context, "This Notes Will Be Added As Secret Notes", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
        }

        ivUnlock.setOnClickListener {
            secret = false
            ivLock.visibility = View.VISIBLE
            ivUnlock.visibility = View.INVISIBLE
            Toast.makeText(context, "Secret Status : ${secret}", Toast.LENGTH_SHORT).show()
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

        btnSubmit.setOnClickListener{
            when {
                edtJudul.text.toString().isEmpty() -> {
                    Toast.makeText(context, "Judul Masih Kosong", Toast.LENGTH_SHORT).show()
                }
                edtDescription.text.toString().isEmpty() -> {
                    Toast.makeText(context, "Catatan Masih Kosong", Toast.LENGTH_SHORT).show() }

                else -> {
                    note.let { note ->
                        note?.title = edtJudul.text.toString()
                        note?.description = edtDescription.text.toString()
                        note?.date = DateHelper.getCurrentDate()
                        note?.idUser = user!!.id
                        note?.image = defaultUri
                        note?.secret = secret
                    }
                    if(defaultUri.equals("Default")) {
                        val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                        viewModel.postNotes(token.toString(),0,note?.title.toString(),note?.description.toString(),DateHelper.getCurrentDate(),user!!.id,"Default")
                        setAdapter()
                        edtJudul.text.clear()
                        edtDescription.text.clear()
                        defaultUri = "Default"
                        dialog.dismiss()
                    } else {
                        val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                        viewModel.postNotes(token.toString(),0,note?.title.toString(),note?.description.toString(),DateHelper.getCurrentDate(),user!!.id,defaultUri)
                        setAdapter()
                        edtJudul.text.clear()
                        edtDescription.text.clear()
                        defaultUri = "Default"
                        dialog.dismiss()
                    }
                    dialog.dismiss()
                    noteAddUpdateViewModel.insert(Note(0,note?.title.toString(),note?.description.toString(),note?.date.toString(),note?.idUser!!.toInt(),imageUri.toString(),note?.secret))
                    setAdapter()
                    Toast.makeText(context, "Berhasil menambahkan satu data ${note!!.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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


        ivLockUpdate.visibility = View.VISIBLE
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
                noteAddUpdateViewModel.update(Note(noteUpdate!!.id,noteUpdate!!.title.toString(),noteUpdate!!.description.toString(),noteUpdate!!.date.toString(),noteUpdate!!.idUser,noteUpdate!!.image.toString(),noteUpdate!!.secret))
                val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                viewModel.updateNote(token.toString(),noteUpdate!!.id,edtTitleUpdate.text.toString(),edtDescription.text.toString(), noteUpdate!!.date.toString(),user!!.id, noteUpdate!!.image.toString())
                Log.d(TAG, "onResumeUpdateHandler: Masuk Btn Submit OnUpdateResume if Attach File ${noteUpdate!!.id}")
                setAdapter()
                dialogUpdate.dismiss()

            } else {
                noteAddUpdateViewModel.update(Note(noteUpdate!!.id,edtTitleUpdate.text.toString(),edtDescription.text.toString(),noteUpdate!!.date.toString(),noteUpdate!!.idUser,imageUri.toString(),secret))
                Log.d(TAG, "onResumeUpdateHandler: Masuk Btn Submit OnUpdateResume if With Image ${noteUpdate!!.id}")
                val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                viewModel.updateNote(token,noteUpdate!!.id,edtTitleUpdate.text.toString(),edtDescription.text.toString(), noteUpdate!!.date.toString(),user!!.id, imageUri.toString())
                setAdapter()
                dialogUpdate.dismiss()

            }
        }
    }
}