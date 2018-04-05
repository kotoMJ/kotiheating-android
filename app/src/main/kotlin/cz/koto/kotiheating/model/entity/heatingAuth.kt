package cz.koto.kotiheating.model.entity

import com.google.gson.annotations.SerializedName

const val HEATING_KEY = "HEATING_KEY"
const val HEATING_SET = "HEATING_SET"
const val USER_KEY = "USER_KEY"

data class HeatingAuthResult(
		@SerializedName("heatingList") val heatingSet: Set<String>? = emptySet(),
		@SerializedName("userKey") val userKey: String? = ""
)