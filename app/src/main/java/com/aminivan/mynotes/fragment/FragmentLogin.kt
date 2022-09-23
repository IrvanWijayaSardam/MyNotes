package com.aminivan.mynotes.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.aminivan.mynotes.R
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.FragmentLoginBinding
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide


class FragmentLogin : Fragment() {

    lateinit var binding : FragmentLoginBinding
    lateinit var dataUserShared : SharedPreferences
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel

    private var user: User? = null
    
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


        Glide.with(this)
            .load(R.drawable.login)
            .into(binding.ivLogin);
        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        dataUserShared = requireActivity().getSharedPreferences("dataUser",Context.MODE_PRIVATE)
        user = User()

        binding.btnLogin.setOnClickListener(){
            observer(binding.edtEmailLogin.text.toString())
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

    fun observer(email : String){
        val mainViewModel = obtainViewModel(requireActivity())
        mainViewModel.authUser(email).observe(requireActivity(), { userData ->
            if (userData != null) {
                user!!.id = userData.id
                user!!.username = userData.username
                user!!.email = userData.email
                user!!.password = userData.password
                submitPref(user!!.id.toString(),user!!.username.toString(),user!!.email.toString(),user!!.password.toString())
                auth(binding.edtPasswordLogin.text.toString())
            } else {
                Toast.makeText(context, "Email Tidak Ditemukan , Silahkan Register", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun auth(password: String){
        if(password.equals(dataUserShared.getString("password","").toString())){
            Toast.makeText(context, "Login Berhasil !!", Toast.LENGTH_SHORT).show()
            gotoHome()
        } else {
            Toast.makeText(context, "Password Salah !!!", Toast.LENGTH_SHORT).show()
        }
    }
    
    
    fun submitPref(id : String,username: String, email: String,password: String){
        var addData = dataUserShared.edit()
        addData.putString("id",id)
        addData.putString("username",username)
        addData.putString("email",email)
        addData.putString("password",password)
        addData.apply()

    }

    fun gotoHome(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentHome)
    }
    fun gotoRegister(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentRegister)
    }

}