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
import com.aminivan.mynotes.response.Data
import com.aminivan.mynotes.response.LoginResponse
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.UserResponseItem
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log


class FragmentLogin : Fragment() {

    lateinit var binding : FragmentLoginBinding
    lateinit var dataUserShared : SharedPreferences
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var encryptor: Encryptor

    private var user: User? = null
    var idUser : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtPasswordLogin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        encryptor = Encryptor()

        Glide.with(this)
            .load(R.drawable.login)
            .into(binding.ivLogin);
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        dataUserShared = requireActivity().getSharedPreferences("dataUser",Context.MODE_PRIVATE)
        user = User()

        binding.btnLogin.setOnClickListener(){
//            observer(binding.edtEmailLogin.text.toString())
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

//    fun observer(email : String){
//        val mainViewModel = obtainViewModel(requireActivity())
//        mainViewModel.authUser(email).observe(requireActivity(), { userData ->
//            if (userData != null) {
//                user!!.id = userData.id
//                user!!.username = userData.username
//                user!!.email = userData.email
//                user!!.password = userData.password
//                submitPref(user!!.id.toString(),user!!.username.toString(),user!!.email.toString(),user!!.password.toString())
//                auth(binding.edtPasswordLogin.text.toString())
//            } else {
//                Toast.makeText(context, "Email Tidak Ditemukan , Silahkan Register", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
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
                        user!!.jk = responseBody.data.profile
                        submitPref(user!!.name.toString(),user!!.email.toString(),responseBody.data.token.toString())
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
    
    
    fun submitPref(username: String, email: String,token: String){
        var addData = dataUserShared.edit()
        addData.putString("username",username)
        addData.putString("email",email)
        addData.putString("token",token)
        addData.apply()

    }

    fun gotoHome(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentHome)
    }
    fun gotoRegister(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentRegister)
    }



}
