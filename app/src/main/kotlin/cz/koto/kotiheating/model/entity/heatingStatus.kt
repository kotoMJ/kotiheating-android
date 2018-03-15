package cz.koto.kotiheating.model.entity

import com.google.gson.annotations.SerializedName
import java.util.*

data class HeatingStatusResult(
		@SerializedName("heatingDeviceStatus") val heatingDeviceStatus: HeatingDeviceStatus
)

data class HeatingDeviceStatus(
		@SerializedName("timestamp") val timestamp: Date,
		@SerializedName("temperature") val temperature: Float,
		@SerializedName("deviceMode") val deviceMode: Int,
		@SerializedName("day") val deviceDay: String,
		@SerializedName("minute") val deviceMinute: Int,
		@SerializedName("hour") val deviceHour: Int,
		@SerializedName("timetable") val timetable: List<Array<Float>>
)