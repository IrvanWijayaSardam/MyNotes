package com.aminivan.mynotes.fragment

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.databinding.FragmentHomeBinding
import com.aminivan.mynotes.databinding.FragmentProfileBinding
import com.aminivan.mynotes.helper.URIPathHelper
import com.aminivan.mynotes.response.DataUpdate
import com.aminivan.mynotes.response.User
import com.aminivan.mynotes.response.UserResponseItem
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class FragmentProfile : Fragment() {


    lateinit var binding : FragmentProfileBinding
    lateinit var dataUserShared : SharedPreferences
    private val pickImage = 100
    lateinit var imageUri : Uri
    lateinit var profile : Bitmap
    lateinit var selectedFile : String
    lateinit var defaultUri : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageUri = Uri.parse("DefaultUri")
        selectedFile = "Attach File"
        defaultUri = "Default"

        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)
        showData()

        binding.btnUploadProfile.setOnClickListener {
            pickImageFromGallery()
        }

        binding.tvEN.setOnClickListener {
            setLocale("en")
            gotoSplash()
        }

        binding.tvID.setOnClickListener {
            setLocale("id")
            gotoSplash()
        }

        binding.btnSave.setOnClickListener {
            updateUser(dataUserShared.getString("token","").toString(),"",dataUserShared.getString("email","").toString(),dataUserShared.getString("username","").toString(),imageUri.toString(),dataUserShared.getString("jk","").toString())
        }
        binding.btnUpdateUser.setOnClickListener {
            if(binding.edtPassword.text!!.isEmpty()){
                updateUser(dataUserShared.getString("token","").toString(),"",binding.edtEmail.text.toString(),binding.edtName.text.toString(),dataUserShared.getString("profile","").toString(),"M")
            } else {
                if(binding.rbMan.isChecked) {
                    updateUser(dataUserShared.getString("token","").toString(),binding.edtPassword.text.toString(),binding.edtEmail.text.toString(),binding.edtName.text.toString(),dataUserShared.getString("profile","").toString(),"M")
                    clearData()
                    gotoSplash()
                } else {
                    updateUser(dataUserShared.getString("token","").toString(),binding.edtPassword.text.toString(),binding.edtEmail.text.toString(),binding.edtName.text.toString(),dataUserShared.getString("profile","").toString(),"W")
                    clearData()
                    gotoSplash()
                }
            }
        }
    }

    fun showData(){
        Glide.with(this).load(dataUserShared.getString("profile","").toString()).circleCrop().into(binding.ivProfile)
        binding.dataUser = User(dataUserShared.getString("jk",""),"",dataUserShared.getString("username",""),dataUserShared.getInt("id",0),dataUserShared.getString("email",""))
        Log.d(TAG, "showData: ${dataUserShared.getString("jk","")}")
        Log.d(TAG, "showData: ${dataUserShared.getString("profile","").toString()}")

        if(dataUserShared.getString("jk","").toString().equals("M")){
            binding.rbMan.isChecked = true
            binding.rbWoman.isChecked = false
        } else {
            binding.rbMan.isChecked = false
            binding.rbWoman.isChecked = true
        }
    }

    private fun pickImageFromGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data!!
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(requireContext(), imageUri)
            profile = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.parse(imageUri.toString()))
            uploadToFirebase()
            Log.d(TAG, "onActivityResult: filepath : ${filePath} ")
            Log.d(TAG, "GetImageUriDefault: ${imageUri.toString()}")
            selectedFile = filePath.toString()
            Log.d(TAG, "onActivityResult bitmap: ${profile}")
        }
    }

    fun uploadToFirebase(){
        val fileName = dataUserShared.getInt("id",0)

        val storageReference = FirebaseStorage.getInstance().getReference("profile/$fileName")

        storageReference.putFile(imageUri).addOnSuccessListener {
            Log.d(TAG, "uploadToFirebase: SUCCESS")
            it.storage.downloadUrl.addOnCompleteListener {
                defaultUri = it.result.toString()
                Log.d("Get Download URL", "Get Download URL : ${it.result.toString()}")
                imageUri = it.result
                Glide.with(this).load(imageUri).circleCrop().into(binding.ivProfile)
                binding.btnSave.visibility = View.VISIBLE
            }.addOnFailureListener {
                Log.d(TAG, "On Failure :${it.message.toString()} ")
            }
        }.addOnFailureListener{
            Log.d(TAG, "uploadToFirebase: YOU'RE SUCH A FAILURE")
        }
    }

    private fun updateUser(token: String,password : String,email:String,name:String,profile: String, Jk: String) {
        val client = ApiConfig.getApiService().updateUser(token, UserResponseItem(password,0,email,name,profile,Jk))
        client.enqueue(object : Callback<UserResponseItem> {
            override fun onResponse(
                call: Call<UserResponseItem>,
                response: Response<UserResponseItem>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                    var addData = dataUserShared.edit()
                    addData.putString("profile",profile)
                    addData.apply()
                    Toast.makeText(context, "Upload Profile Success", Toast.LENGTH_SHORT).show()
                    binding.btnSave.visibility = View.GONE
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponseItem>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }

    fun gotoSplash(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentProfile_to_fragmentSplash)
    }

    fun setLocale(lang: String?) {
        val myLocale = Locale(lang)
        val res = resources
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, res.displayMetrics)
    }

    fun clearData(){
        var pref = dataUserShared.edit()
        pref.clear()
        pref.apply()
    }
}