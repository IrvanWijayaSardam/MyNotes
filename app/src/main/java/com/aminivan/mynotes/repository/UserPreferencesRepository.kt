package com.aminivan.mynotes.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.aminivan.mynotes.UserDataProto
import com.aminivan.mynotes.proto.UserPreferencesSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

    private val Context.userPreferencesStore: DataStore<UserDataProto> by dataStore(
        fileName = "userData",
        serializer = UserPreferencesSerializer
    )
class UserPreferencesRepository(private val context: Context) {

    suspend fun saveData(id : Int,name : String,email : String,password : String,profile : String,jk : String, token : String) {
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setId(id).build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setName(name).build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setEmail(email).build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setPassword(password).build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setProfile(profile).build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setJk(jk).build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setToken(token).build()
        }
    }

    //    delete datastore proto

    suspend fun deleteData() {
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearId().build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearName().build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearPassword().build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearEmail().build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearProfile().build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearJk().build()
        }
        context.userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().clearToken().build()
        }
    }

//    read data store proto

    val readProto: Flow<UserDataProto> = context.userPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e("tag", "Error reading sort order preferences.", exception)
                emit(UserDataProto.getDefaultInstance())
            } else {
                throw exception
            }
        }

}