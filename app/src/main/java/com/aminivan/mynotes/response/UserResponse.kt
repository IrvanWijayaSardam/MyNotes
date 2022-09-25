package com.aminivan.mynotes.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

	@field:SerializedName("UserResponseItem")
	val userResponseItem: UserResponseItem
)

data class UserResponseItem(

	@field:SerializedName("password")
	val password: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("username")
	val username: String
)
