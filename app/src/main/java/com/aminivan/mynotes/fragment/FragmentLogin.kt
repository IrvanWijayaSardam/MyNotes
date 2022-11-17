package com.aminivan.mynotes.fragment

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
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
import androidx.navigation.fragment.findNavController
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.FragmentLoginBinding
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.aminivan.mynotes.helper.Encryptor
import com.aminivan.mynotes.response.*
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.NotesViewModel
import com.aminivan.mynotes.viewmodel.UserViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.aminivan.mynotes.workers.sleep
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

@AndroidEntryPoint
class FragmentLogin : Fragment() {

    private var _binding : FragmentLoginBinding?                        = null
    private val binding get()                                           = _binding!!
    private var note: Note?                                             = null
    private var user: User?                                             = null
    var status : Boolean                                                = false
    var idUser : Int                                                    = 0
    private lateinit var noteAddUpdateViewModel : NoteAddUpdateViewModel
    lateinit var encryptor: Encryptor
    lateinit var viewModeluser : UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtPasswordLogin.inputType  = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        viewModeluser                       = ViewModelProvider(this).get(UserViewModel::class.java)
        encryptor                           = Encryptor()
        note                                = Note()
        user                                = User()
        val viewModel                       = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)

        Glide.with(this)
            .load(R.drawable.login)
            .into(binding.ivLogin);
        noteAddUpdateViewModel = obtainViewModel(requireActivity())

        binding.btnLogin.setOnClickListener(){
            if(binding.edtEmailLogin.text.toString().isEmpty()) {
                binding.edtEmailLogin.error = "Silahkan isi email"
            } else if (binding.edtPasswordLogin.text.toString().isEmpty()){
                binding.edtPasswordLogin.error = "Silahkan isi password"
            } else {
                viewModel.authApi(binding.edtEmailLogin.text.toString(), binding.edtPasswordLogin.text.toString())
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

        viewModeluser.dataUser.observe(viewLifecycleOwner,{
            Log.d(TAG, "onViewCreatedSplash: ${it.email.toString()}")
            Log.d(TAG, "onViewCreated: userData ${user?.email}")
            user!!.email = it.email
        })

        viewModel.getDataUser().observe(viewLifecycleOwner, {
            Log.d(TAG, "onViewCreated ObserverDataUser: $it")
            if (it != null){
                if(it!!.status == false ){
                    showSnack("Username / Password Salah")
                } else {
                    if(it!!.data != null){
                        gotoHome()
                    }
                }
            } else {
                showSnack("Username / Password Salah")
            }
        })

    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun gotoHome(){
        val bundle = Bundle()
        bundle.putString("password",binding.edtPasswordLogin.text.toString())
        findNavController().navigate(R.id.action_fragmentLogin_to_fragmentHome,bundle)
    }
    fun gotoRegister(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentRegister)
    }

    fun showSnack(message: String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

}
