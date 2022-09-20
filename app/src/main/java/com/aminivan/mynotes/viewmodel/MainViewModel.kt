package com.aminivan.mynotes.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.repository.NoteRepository

class MainViewModel (application: Application) : ViewModel() {
    private val mNoteRepository: NoteRepository = NoteRepository(application)
}