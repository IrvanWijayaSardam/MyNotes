package com.aminivan.mynotes.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.repository.NoteRepository
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.PostNotesResponse
import com.aminivan.mynotes.response.ResponseFetchAll
import com.aminivan.mynotes.response.UpdateNotesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoteAddUpdateViewModel(application: Application) : ViewModel() {
    private val mNoteRepository: NoteRepository = NoteRepository(application)
    fun insert(note: Note) {
        mNoteRepository.insert(note)
    }
    fun update(note: Note) {
        mNoteRepository.update(note)
    }
    fun delete(note: Note) {
        mNoteRepository.delete(note)
    }
    fun getAllNotes(idUser: String,secret : Boolean): LiveData<List<Note>> = mNoteRepository.getAllNotes(idUser,secret)

    fun deleteAllNotes(){
        mNoteRepository.deleteAllNotes()
    }


    fun insertUser(user: User){
        mNoteRepository.insertUser(user)
    }
    fun updateUser(user: User){
        mNoteRepository.updateUser(user)
    }
    fun deleteUser(user: User){
        mNoteRepository.deleteUser(user)
    }
    fun authUser(email : String): LiveData<User> = mNoteRepository.authUser(email)


    fun postNotes(token : String,id: Int,title:String,description:String,date: String,userid : Int, image : String) {
        val client = ApiConfig.getApiService().createNotes(token,
            NoteResponseItem(id,title,description,date, userid,image)
        )
        client.enqueue(object : Callback<PostNotesResponse> {
            override fun onResponse(
                call: Call<PostNotesResponse>,
                response: Response<PostNotesResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.d(TAG, "onResponse: Note Inserted")
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
        val client = ApiConfig.getApiService().updateNotes(token,id.toString(),
            NoteResponseItem(id, title, description, date, userid, image))
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
        val client = ApiConfig.getApiService().deleteNotes(token,id.toString())
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

}