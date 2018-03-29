package cz.koto.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName
import java.util.*

data class HeatingStatusResult(
		@SerializedName("result") val heatingDeviceStatus: HeatingDeviceStatus
)

@Entity(tableName = "deviceStatus", primaryKeys = ["deviceId"])
data class HeatingDeviceStatus(
		@SerializedName("timestamp") val timestamp: Date,
		@SerializedName("temperature") val temperature: Float,
		@SerializedName("deviceMode") val deviceMode: Int,
		@SerializedName("day") val deviceDay: String,
		@SerializedName("minute") val deviceMinute: Int,
		@SerializedName("hour") val deviceHour: Int,
		@SerializedName("timetable") val timetable: List<List<Float>>,
		@SerializedName("deviceId") val deviceId: String
)