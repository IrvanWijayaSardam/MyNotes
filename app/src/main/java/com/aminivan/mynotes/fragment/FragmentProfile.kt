package com.aminivan.mynotes.fragment

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aminivan.mynotes.R
import com.aminivan.mynotes.databinding.FragmentHomeBinding
import com.aminivan.mynotes.databinding.FragmentProfileBinding
import com.aminivan.mynotes.response.User
import com.bumptech.glide.Glide


class FragmentProfile : Fragment() {


    lateinit var binding : FragmentProfileBinding
    lateinit var dataUserShared : SharedPreferences

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

        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)
        showData()
    }

    fun showData(){
        Glide.with(this).load(dataUserShared.getString("profile","").toString()).circleCrop().into(binding.ivProfile)
        binding.dataUser = User(dataUserShared.getString("jk",""),"",dataUserShared.getString("username",""),dataUserShared.getInt("id",0),dataUserShared.getString("email",""))
        Log.d(TAG, "showData: ${dataUserShared.getString("profile","").toString()}")
    }


}