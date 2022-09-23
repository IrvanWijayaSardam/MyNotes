package com.aminivan.mynotes.fragment

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
import com.aminivan.mynotes.databinding.FragmentRegisterBinding
import com.aminivan.mynotes.helper.DateHelper
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide


class FragmentRegister : Fragment() {
    lateinit var binding : FragmentRegisterBinding
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel

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
                else -> {
                    user.let { note ->
                        note?.username = binding.edtUsername.text.toString()
                        note?.email = binding.edtEmail.text.toString()
                        note?.password = binding.edtPassword.text.toString()
                    }
                    noteAddUpdateViewModel.insertUser(user as User)
                    Toast.makeText(context, "Registrasi Berhasil Silahkan Login", Toast.LENGTH_SHORT).show()
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

    }
    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }

    fun gotoLogin(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentRegister_to_fragmentLogin)
    }

}