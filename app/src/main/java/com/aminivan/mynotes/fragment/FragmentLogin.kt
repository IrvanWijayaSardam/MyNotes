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
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

@AndroidEntryPoint
class FragmentLogin : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var encryptor: Encryptor
    lateinit var viewModeluser : UserViewModel
    private var note: Note? = null
    private var user: User? = null
    var status : Boolean = false
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
                val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
                viewModel.authApi(binding.edtEmailLogin.text.toString(), binding.edtPasswordLogin.text.toString())
                viewModel.getLiveDataUsers().observe(viewLifecycleOwner,{
                    Log.d(TAG, "onViewCreated: Observe${it}")
                    if(status == false) {
                        if(it == null) {
                            Log.d(TAG, "onViewCreated It Null: Didalam Null ")
                            Toast.makeText(requireContext(), "Username / Password Salah If Null", Toast.LENGTH_SHORT).show()
                        } else {
                            if(it!!.status == false){
                                Toast.makeText(requireContext(), "Username / Password salah If Status False", Toast.LENGTH_SHORT).show()
                            } else {
                                if(it!!.data!!.email.equals(binding.edtEmailLogin.text.toString())){
                                    status = true
                                    gotoHome()
                                    Log.d(TAG, "onViewCreated: Found Executed")
                                    Toast.makeText(requireContext(), "Login berhasil", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Username / Password salah If Status False", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                })
                Log.d(TAG, "onViewCreated: clicked status ${status}")
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

    fun gotoHome(){
        findNavController().navigate(R.id.action_fragmentLogin_to_fragmentHome)
    }
    fun gotoRegister(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentLogin_to_fragmentRegister)
    }
}
