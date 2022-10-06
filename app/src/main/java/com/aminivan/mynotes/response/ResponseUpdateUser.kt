package com.aminivan.mynotes.response

import com.google.gson.annotations.SerializedName

data class ResponseUpdateUser(

	@field:SerializedName("data")
	val data: DataUpdate? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("errors")
	val errors: Any? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class DataUpdate(

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
