package com.aminivan.mynotes.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(note: Note)
    @Update
    fun update(note: Note)
    @Delete
    fun delete(note: Note)
    @Query("SELECT * from note WHERE idUser LIKE:idUser AND secret LIKE:secret ORDER BY id ASC ")
    fun getAllNotes(idUser: String, secret : Boolean): LiveData<List<Note>>

    @Query("DELETE FROM note")
    fun deleteAllNotes()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: User)
    @Update
    fun updateUser(user: User)
    @Delete
    fun deleteUser(user: User)
    @Query("SELECT * from user WHERE email LIKE :email")
    fun authUser(email : String): LiveData<User>

}