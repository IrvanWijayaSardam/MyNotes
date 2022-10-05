package com.aminivan.mynotes.response

import com.google.gson.annotations.SerializedName

data class ResponseFetchAll(

	@field:SerializedName("data")
	val data: DataNotes? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("errors")
	val errors: Any? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class DataNotes(

	@field:SerializedName("jk")
	val jk: String? = null,

	@field:SerializedName("notes")
	val notes: List<NotesItem?>? = null,

	@field:SerializedName("profile")
	val profile: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null
)

data class NotesItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("user")
	val user: User? = null
)

data class User(

	@field:SerializedName("jk")
	val jk: String? = null,

	@field:SerializedName("profile")
	val profile: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null
)
