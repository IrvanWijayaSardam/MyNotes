package com.aminivan.mynotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminivan.mynotes.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = UserPreferencesRepository(application)
    val dataUser = repo.readProto.asLiveData()

    fun editData(id : Int,name : String,email : String,password : String,profile : String,jk : String, token : String) = viewModelScope.launch {
        repo.saveData(id,name,email,password,profile,jk,token)
    }

    fun clearData() = viewModelScope.launch{
        repo.deleteData()
    }
}