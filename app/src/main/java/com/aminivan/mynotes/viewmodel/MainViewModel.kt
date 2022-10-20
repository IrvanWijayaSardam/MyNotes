package com.aminivan.mynotes.viewmodel

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aminivan.mynotes.config.ApiConfig
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.repository.NoteRepository
import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.PostNotesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel (application: Application) : ViewModel() {
    private val mNoteRepository: NoteRepository = NoteRepository(application)

}