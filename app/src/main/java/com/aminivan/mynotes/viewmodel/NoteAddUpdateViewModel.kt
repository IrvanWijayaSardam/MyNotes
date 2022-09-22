package com.aminivan.mynotes.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.repository.NoteRepository

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
    fun getAllNotes(idUser: String): LiveData<List<Note>> = mNoteRepository.getAllNotes(idUser)

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


}