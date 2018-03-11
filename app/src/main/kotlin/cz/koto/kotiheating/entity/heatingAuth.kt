package cz.koto.kotiheating.entity

import com.google.gson.annotations.SerializedName


data class HeatingAuthResult(
		@SerializedName("heatingKey") val heatingKey: String,
		@SerializedName("userKey") val userKey: String
)