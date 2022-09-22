package com.aminivan.mynotes.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.database.NoteDao
import com.aminivan.mynotes.database.NoteRoomDatabase
import com.aminivan.mynotes.database.User
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NoteRepository(application: Application) {
    private val mNotesDao: NoteDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    init {
        val db = NoteRoomDatabase.getDatabase(application)
        mNotesDao = db.noteDao()
    }
    fun getAllNotes(idUser: String): LiveData<List<Note>> = mNotesDao.getAllNotes(idUser)
    fun insert(note: Note) {
        executorService.execute { mNotesDao.insert(note) }
    }
    fun delete(note: Note) {
        executorService.execute { mNotesDao.delete(note) }
    }
    fun update(note: Note) {
        executorService.execute { mNotesDao.update(note) }
    }

    fun insertUser(user: User) {
        executorService.execute{mNotesDao.insertUser(user)}
    }
    fun deleteUser(user : User) {
        executorService.execute{mNotesDao.deleteUser(user)}
    }
    fun updateUser(user : User) {
        executorService.execute { mNotesDao.updateUser(user) }
    }
    fun authUser(email : String): LiveData<User> = mNotesDao.authUser(email)

}