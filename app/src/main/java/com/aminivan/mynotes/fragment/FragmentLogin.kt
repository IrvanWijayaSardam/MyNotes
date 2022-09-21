package com.aminivan.mynotes.fragment

import android.os.Bundle
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
        Glide.with(this)
            .load(R.drawable.login)
            .into(binding.ivLogin);

        user = User()

        binding.btnLogin.setOnClickListener(){
            observer(binding.edtEmailLogin.text.toString())
        }

        binding.tvGotoRegister.setOnClickListener(){
            gotoRegister()
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
                Toast.makeText(context, "ID : ${user!!.id}", Toast.LENGTH_SHORT).show()
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