package com.aminivan.mynotes.fragment

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.databinding.FragmentHomeBinding
import com.aminivan.mynotes.databinding.FragmentLoginBinding
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.aminivan.mynotes.response.DataNotes
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.NotesItem
import com.aminivan.mynotes.response.ResponseFetchAll
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.UserViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentSplash : Fragment() {

    private var _binding : FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    private var note: Note? = null
    private var user : User? = null
    lateinit var viewModeluser : UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        note = Note()
        user = User()
        viewModeluser = ViewModelProvider(this).get(UserViewModel::class.java)

        Glide.with(this)
            .load(R.drawable.notebook)
            .into(binding.ivSplash);
        noteAddUpdateViewModel = obtainViewModel(requireActivity())

        android.os.Handler(Looper.myLooper()!!).postDelayed({
            var token : String = ""
            viewModeluser.dataUser.observe(viewLifecycleOwner,{
                    user!!.id = it.id
                    user!!.name = it.name
                    user!!.email = it.email
                    user!!.password = it.password
                    user!!.profile = it.profile
                    user!!.jk = it.jk
                    Log.d(TAG, "onViewCreated: userData ${user?.email}")
                    if(user?.email?.length == 0){
                        gotoLogin()
                    } else {
                        gotoHome()
                    }
            })
        },5000)
    }

    fun gotoLogin(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentSplash_to_fragmentLogin)
    }
    fun gotoHome(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentSplash_to_fragmentHome)
    }
    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }
    
}