package com.aminivan.mynotes.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

    @field:SerializedName("NoteResponse")
    val noteResponse: List<NoteResponseItem>
)

data class NoteResponseItem(

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("userid")
    val userid: String,
)

data class PostUserResponse(

    @field:SerializedName("listUser")
    val listUsers: List<NoteResponseItem>

)

data class UpdateUserResponse(
    @field:SerializedName("updatedUser")
    val updatedUser: NoteResponseItem
)