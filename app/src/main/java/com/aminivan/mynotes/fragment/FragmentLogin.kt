package com.aminivan.mynotes.fragment

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.FragmentLoginBinding
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.aminivan.mynotes.helper.Encryptor
import com.aminivan.mynotes.response.*
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.UserViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log


class FragmentLogin : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var encryptor: Encryptor
    lateinit var viewModeluser : UserViewModel
    private var note: Note? = null
    private var user: User? = null
    var idUser : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtPasswordLogin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        viewModeluser = ViewModelProvider(this).get(UserViewModel::class.java)

        encryptor = Encryptor()

        Glide.with(this)
            .load(R.drawable.login)
            .into(binding.ivLogin);
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        note = Note()
        user = User()

        binding.btnLogin.setOnClickListener(){
            if(binding.edtEmailLogin.text.toString().isEmpty()) {
                binding.edtEmailLogin.error = "Silahkan isi email"
            } else if (binding.edtPasswordLogin.text.toString().isEmpty()){
                binding.edtPasswordLogin.error = "Silahkan isi password"
            } else {
                authApi(binding.edtEmailLogin.text.toString(),binding.edtPasswordLogin.text.toString())
            }
        }

        binding.tvGotoRegister.setOnClickListener(){
            gotoRegister()
        }

        binding.ivEyeLogin.setOnClickListener {
            if(binding.edtPasswordLogin.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                binding.edtPasswordLogin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.edtPasswordLogin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun authApi(email : String,password: String){
        val client = ApiConfig.getApiService().auth(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d(TAG, "UserToken: ${responseBody}")
                        idUser = responseBody.data!!.id!!.toInt()
                        user!!.id = responseBody.data!!.id!!.toInt()
                        user!!.email = responseBody.data.email
                        user!!.name = responseBody.data.name
                        user!!.profile = responseBody.data.profile
                        user!!.jk = responseBody.data.jk
                        noteAddUpdateViewModel.deleteAllNotes()
                        viewModeluser.editData(user!!.id,
                            user!!.name.toString(),user!!.email.toString(),password,user!!.profile.toString(),responseBody.data.jk.toString(),responseBody.data.token.toString())
                        retriveNotes(responseBody.data.token.toString())
                        Log.d(TAG, "UserToken: ${responseBody.data}")
                        gotoHome()
                    }
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                    if (response.message().equals("Unauthorized")){
                        Toast.makeText(context, "Email / Password salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
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
                                    responseBody[i]!!.date,responseBody[i]!!.user!!.id!!.toInt(),responseBody[i]!!.image,false))
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


    fun gotoHome(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentHome)
    }
    fun gotoRegister(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentRegister)
    }
}
