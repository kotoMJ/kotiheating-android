package cz.koto.kotiheating.model.entity

import com.google.gson.annotations.SerializedName

const val HEATING_KEY = "HEATING_KEY"
const val USER_KEY = "USER_KEY"

data class HeatingAuthResult(
		@SerializedName("heatingKey") val heatingKey: String,
		@SerializedName("userKey") val userKey: String
)