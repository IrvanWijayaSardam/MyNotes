package com.aminivan.mynotes.fragment

import android.content.ContentValues
import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.FragmentRegisterBinding
import com.aminivan.mynotes.helper.Encryptor
import com.aminivan.mynotes.response.Data
import com.aminivan.mynotes.response.LoginResponse
import com.aminivan.mynotes.response.PostUserResponse
import com.aminivan.mynotes.response.UserResponseItem
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.NotesViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentRegister : Fragment() {
    lateinit var binding : FragmentRegisterBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    lateinit var Jk: String
    private var user: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.edtRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val encryptor = Encryptor()
        Jk = "N"

        Glide.with(this)
            .load(R.drawable.document)
            .into(binding.ivRegister);

        noteAddUpdateViewModel = obtainViewModel(requireActivity())
        user = User()

        binding.btnRegister.setOnClickListener(){
            when {
                binding.edtUsername.text.toString().isEmpty() -> {
                    binding.edtUsername.error = "Data Username Tidak Boleh Kosong !!"
                }
                binding.edtEmail.text.toString().isEmpty() -> {
                    binding.edtEmail.error = "Data Tidak Boleh Kosong !!"
                }
                binding.edtPassword.text.toString().isEmpty() -> {
                    binding.edtPassword.error = "Data Tidak Boleh Kosong !!"
                }
                binding.edtPassword.text.toString() != binding.edtRepeatPassword.text.toString() -> {
                    binding.edtRepeatPassword.error = "Password tidak sama !!"
                }
                Jk.equals("N")-> {
                    Toast.makeText(context, "Silahkan pilih jenis kelamin", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    user.let { note ->
                        note?.name = binding.edtUsername.text.toString()
                        note?.email = binding.edtEmail.text.toString()
                        note?.password = encryptor.encryptAndSavePassword(requireContext(),binding.edtPassword.text.toString()).toString()
                    }
                    noteAddUpdateViewModel.insertUser(user as User)
                    registUser(User(0,binding.edtUsername.text.toString(),binding.edtEmail.text.toString(),binding.edtPassword.text.toString(),"default",Jk))
                    Toast.makeText(context, "Registrasi Berhasil Silahkan Login", Toast.LENGTH_SHORT).show()
                    noteAddUpdateViewModel.deleteAllNotes()
                    gotoLogin()
                }
            }
        }

        binding.ivEye.setOnClickListener{
            if (binding.edtPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD){
                binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

        }
        binding.ivEyeConfirm.setOnClickListener{
            if (binding.edtRepeatPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD){
                binding.edtRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.edtRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        binding.tvLogin.setOnClickListener{
            gotoLogin()
        }

        binding.cvMan.setOnClickListener {
            binding.cvMan.setCardBackgroundColor(Color.YELLOW)
            binding.cvWoman.setCardBackgroundColor(Color.WHITE)
            Jk = "M"
        }

        binding.cvWoman.setOnClickListener {
            binding.cvMan.setCardBackgroundColor(Color.WHITE)
            binding.cvWoman.setCardBackgroundColor(Color.YELLOW)
            Jk = "W"
        }

    }

    private fun registUser(user: User) {
        val viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)
        viewModel.registUser(user)
    }

    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun gotoLogin(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentRegister_to_fragmentLogin)
    }

}