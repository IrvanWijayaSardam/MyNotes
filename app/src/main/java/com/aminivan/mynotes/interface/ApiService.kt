package com.aminivan.mynotes.`interface`

import com.aminivan.mynotes.response.NoteResponseItem
import com.aminivan.mynotes.response.PostUserResponse
import com.aminivan.mynotes.response.UpdateUserResponse
import com.aminivan.mynotes.response.UserResponseItem
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("notes/")
    fun getNotes(
    ): Call<List<NoteResponseItem>>

    @POST("notes/")
    fun createNotes(
        @Body body: NoteResponseItem
    ) : Call<PostUserResponse>

    @PUT("notes/{id}")
    fun updateNotes(
        @Path("id") id : String,
        @Body body: NoteResponseItem
    ): Call<UpdateUserResponse>

    @POST("user")
    fun createUser(
        @Body body :UserResponseItem
    ) : Call<PostUserResponse>

    @GET("auth/{email}")
    fun getUser(
        @Path("email") email : String,
    ): Call<UserResponseItem>

}