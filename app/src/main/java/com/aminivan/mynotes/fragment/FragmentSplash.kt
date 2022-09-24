package com.aminivan.mynotes.fragment

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aminivan.mynotes.R
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.aminivan.mynotes.response.NoteResponseItem
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentSplash : Fragment() {

    lateinit var binding : FragmentSplashBinding
    lateinit var dataUserShared : SharedPreferences


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
        retriveNotes()
        Glide.with(this)
            .load(R.drawable.notebook)
            .into(binding.ivSplash);
        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)

        android.os.Handler(Looper.myLooper()!!).postDelayed({
            if(dataUserShared.getString("id","").equals("")){
                gotoLogin()
            } else {
                gotoHome()
            }
        },5000)
    }

    fun gotoLogin(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentSplash_to_fragmentLogin)
    }
    fun gotoHome(){
        Navigation.findNavController(requireView()).navigate(R.id.action_fragmentSplash_to_fragmentHome)
    }

    private fun retriveNotes() {
        val client = ApiConfig.getApiService().getusers()
        client.enqueue(object : Callback<List<NoteResponseItem>> {
            override fun onResponse(
                call: Call<List<NoteResponseItem>>,
                response: Response<List<NoteResponseItem>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Toast.makeText(context, "${responseBody}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<List<NoteResponseItem>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }


}