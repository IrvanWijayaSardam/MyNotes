package com.aminivan.mynotes.fragment

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.aminivan.mynotes.R
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.bumptech.glide.Glide

class FragmentSplash : Fragment() {

    lateinit var binding : FragmentSplashBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this)
            .load(R.drawable.notebook)
            .into(binding.ivSplash);

        android.os.Handler(Looper.myLooper()!!).postDelayed({
            gotoLogin()
        },5000)
    }

    fun gotoLogin(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentSplash_to_fragmentLogin)
    }


}