package com.aminivan.mynotes.viewmodel

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aminivan.mynotes.UserDataProto
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.service.ApiService
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.response.*
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class NotesViewModel @Inject constructor(var api : ApiService) : ViewModel() {

    var liveDataUser = MutableLiveData<LoginResponse?>()
    var dataUserResponse : LiveData<LoginResponse?> = liveDataUser

    lateinit var liveDataNotes : MutableLiveData<ResponseFetchAll?>

    init {
        liveDataNotes = MutableLiveData()
    }

    fun getLiveDataUsers() : LiveData<LoginResponse?> {
        return dataUserResponse
    }

    fun getDataUser() : LiveData<LoginResponse?>{
        return dataUserResponse
    }

    fun getLiveDataNote() : MutableLiveData<ResponseFetchAll?> {
        return liveDataNotes
    }

    fun setNullNotes(){
        liveDataNotes.value = null
    }

    fun setNullUser(){
        liveDataUser.value = null
    }

    fun authApi(email : String,password: String){
        Log.d(TAG, "authApi: Executed")
        val client = api.auth(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null){
                    liveDataUser.value = responseBody
                } else {
                    liveDataUser.value = null
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                liveDataUser.value = null
            }
        })
    }

    fun retriveNotes(token : String) {
        val client = api.getNotes(token)
        client.enqueue(object : Callback<ResponseFetchAll> {
            override fun onResponse(
                call: Call<ResponseFetchAll>,
                response: Response<ResponseFetchAll>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!.data!!.notes
                    if (responseBody != null) {
                        Log.d(TAG, "onResponse: ${responseBody}")
                        liveDataNotes.postValue(response.body())
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

    fun postNotes(token : String,id: Int,title:String,description:String,date: String,userid : Int, image : String) {
        val client = api.createNotes(token,
            NoteResponseItem(id,title,description,date, userid,image)
        )
        client.enqueue(object : Callback<PostNotesResponse> {
            override fun onResponse(
                call: Call<PostNotesResponse>,
                response: Response<PostNotesResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.d(ContentValues.TAG, "onResponse: Note Inserted")
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                    //dialog.dismiss()
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                    Log.d(ContentValues.TAG, "onResponse: ${token}")
                    Log.d(ContentValues.TAG, "onResponse: ${id}")
                    Log.d(ContentValues.TAG, "onResponse: ${title}")
                    Log.d(ContentValues.TAG, "onResponse: ${description}")
                    Log.d(ContentValues.TAG, "onResponse: ${date}")
                    Log.d(ContentValues.TAG, "onResponse: ${image}")
                }
            }

            override fun onFailure(call: Call<PostNotesResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun updateNote(token: String,id: Int,title: String,description: String,date: String,userid: Int,image: String){
        val client = api.updateNotes(token,id.toString(),
            NoteResponseItem(id, title, description, date, userid, image)
        )
        client.enqueue(object : Callback<UpdateNotesResponse> {
            override fun onResponse(
                call: Call<UpdateNotesResponse>,
                response: Response<UpdateNotesResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<UpdateNotesResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }

    fun deleteNote(token: String,id: Int) {
        val client = api.deleteNotes(token,id.toString())
        client.enqueue(object : Callback<ResponseFetchAll> {
            override fun onResponse(
                call: Call<ResponseFetchAll>,
                response: Response<ResponseFetchAll>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseFetchAll>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun registUser(user: User) {
        val client = api.createUser(user)
        client.enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.d(ContentValues.TAG, "onResponse: uSER Inserted")
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                    //dialog.dismiss()
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun updateUser(token: String,password : String,email:String,name:String,profile: String, Jk: String) {
        val client = api.updateUser(token, UserResponseItem(password,0,email,name,profile,Jk))
        client.enqueue(object : Callback<UserResponseItem> {
            override fun onResponse(
                call: Call<UserResponseItem>,
                response: Response<UserResponseItem>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(ContentValues.TAG, "onSuccess: ${responseBody}")
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponseItem>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message}")
            }

        })
    }


}