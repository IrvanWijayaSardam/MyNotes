package com.aminivan.mynotes.`interface`

import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.response.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("notes/")
    fun getNotes(
    ): Call<List<NoteResponseItem>>

    @GET("notes/{id}")
    fun getNotesById(
        @Path("id") id: String,
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

    @POST("api/auth/register")
    fun createUser(
        @Body body :User
    ) : Call<User>

    @GET("auth/{email}")
    fun getUser(
        @Path("email") email : String,
    ): Call<UserResponseItem>

    @FormUrlEncoded
    @POST("api/auth/login")
    fun auth(
        @Field("email") email: String,
        @Field("password") password : String
    ):Call<LoginResponse>


}