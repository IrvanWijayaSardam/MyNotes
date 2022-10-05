package com.aminivan.mynotes.response

import com.google.gson.annotations.SerializedName

data class NoteResponse(

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
    val userid: Int,

    @field:SerializedName("image")
    val image: String,
)

data class PostNotesResponse(

    @field:SerializedName("listNotes")
    val listNotes: List<NoteResponseItem>

)