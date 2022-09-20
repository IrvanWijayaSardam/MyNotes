package com.aminivan.mynotes.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aminivan.mynotes.R
import com.aminivan.mynotes.databinding.FragmentLoginBinding
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.bumptech.glide.Glide


class FragmentLogin : Fragment() {

    lateinit var binding : FragmentLoginBinding

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
    }

}