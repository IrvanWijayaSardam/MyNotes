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
import com.aminivan.mynotes.databinding.FragmentSplashBinding
import com.aminivan.mynotes.response.DataNotes
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.NotesItem
import com.aminivan.mynotes.response.ResponseFetchAll
import com.aminivan.mynotes.viewmodel.NoteAddUpdateViewModel
import com.aminivan.mynotes.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentSplash : Fragment() {

    lateinit var binding : FragmentSplashBinding
    lateinit var dataUserShared : SharedPreferences
    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    private var note: Note? = null


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
        note = Note()

        Glide.with(this)
            .load(R.drawable.notebook)
            .into(binding.ivSplash);
        dataUserShared = requireActivity().getSharedPreferences("dataUser", Context.MODE_PRIVATE)
        noteAddUpdateViewModel = obtainViewModel(requireActivity())

        android.os.Handler(Looper.myLooper()!!).postDelayed({
            if(dataUserShared.getInt("id",0).equals("")){
                gotoLogin()
            } else {
                noteAddUpdateViewModel.deleteAllNotes()
                retriveNotes(dataUserShared.getInt("id",0).toString())
                Toast.makeText(context, "All Notes Deleted", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onViewCreated: ${dataUserShared.getString("token","").toString()}")
                retriveNotes(dataUserShared.getString("token","").toString())
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
    private fun obtainViewModel(activity: FragmentActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }
    
    private fun retriveNotes(token : String) {
        val client = ApiConfig.getApiService().getNotes(token)
        client.enqueue(object : Callback<ResponseFetchAll> {
            override fun onResponse(
                call: Call<ResponseFetchAll>,
                response: Response<ResponseFetchAll>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!.data!!.notes
                    if (responseBody != null) {
                        Log.d(TAG, "onResponse: ${responseBody}")
                        for (i in 0 until responseBody.size) {
                            note.let { note ->
                                note?.id = responseBody[i]!!.id!!.toInt()
                                note?.title = responseBody[i]!!.title
                                note?.description = responseBody[i]!!.description
                                note?.date = responseBody[i]!!.date
                                note?.idUser = responseBody[i]!!.user!!.id!!.toInt()
                                note?.image = responseBody[i]!!.image
                                noteAddUpdateViewModel.insert(Note(responseBody[i]!!.id!!.toInt(),responseBody[i]!!.title,responseBody[i]!!.description,
                                    responseBody[i]!!.date,responseBody[i]!!.user!!.id!!.toInt(),responseBody[i]!!.image))
                            }
//                            Log.d(TAG, "onResponse: ${responseBody.size.toString()}")
//                            Log.d(TAG, "onResponse: ${responseBody[i]!!.description}")
                        }
                    }
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<ResponseFetchAll>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }
}