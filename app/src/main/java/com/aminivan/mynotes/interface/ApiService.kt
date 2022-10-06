package com.aminivan.mynotes.`interface`

import com.aminivan.mynotes.database.User
import com.aminivan.mynotes.response.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("api/user/profile")
    fun getNotes(
        @Header("Authorization") authorization : String,
        ): Call<ResponseFetchAll>

    @GET("notes/{id}")
    fun getNotesById(
        @Path("id") id: String,
    ): Call<List<NoteResponseItem>>


    @POST("api/notes/")
    fun createNotes(
        @Header("Authorization") authorization : String,
        @Body body: NoteResponseItem
    ) : Call<PostNotesResponse>

    @PUT("api/notes/{id}")
    fun updateNotes(
        @Header("Authorization") authorization : String,
        @Path("id") id : String,
        @Body body: NoteResponseItem
    ): Call<UpdateNotesResponse>

    @DELETE("api/notes/{id}")
    fun deleteNotes(
        @Header("Authorization") authorization : String,
        @Path("id") id : String,
    ) : Call<ResponseFetchAll>

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