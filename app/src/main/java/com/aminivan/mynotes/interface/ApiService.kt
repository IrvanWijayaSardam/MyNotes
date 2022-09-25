package com.aminivan.mynotes.`interface`

import com.aminivan.mynotes.response.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("notes/")
    fun getNotes(
    ): Call<List<NoteResponseItem>>

    @POST("notes/")
    fun createNotes(
        @Body body: NoteResponseItem
    ) : Call<PostNotesResponse>

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