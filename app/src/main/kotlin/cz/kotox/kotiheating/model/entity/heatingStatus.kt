package cz.kotox.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.Date

data class HeatingStatusResult(
	@SerializedName("result") val heatingDeviceStatus: HeatingDeviceStatus
)

@Entity(tableName = "deviceStatus", primaryKeys = ["deviceId"])
@JvmSuppressWildcards
data class HeatingDeviceStatus(
	@SerializedName("timestamp") val timestamp: Date,
	@SerializedName("temperature") val temperature: Int,
	@SerializedName("heatingMode") val deviceMode: Int,
	@SerializedName("day") val deviceDay: String,
	@SerializedName("minute") val deviceMinute: Int,
	@SerializedName("hour") val deviceHour: Int,
	@SerializedName("timetableDevice") val timetableDevice: MutableList<MutableList<Int>>,
	@SerializedName("timetableServer") val timetableServer: MutableList<MutableList<Int>>,
	//@Transient var timetableLocal: MutableList<MutableList<Int>>?,
	//@Ignore var timetableLocal: MutableList<MutableList<Int>>?,
	@Expose var timetableLocal: MutableList<MutableList<Int>>? = mutableListOf(),
	@SerializedName("heatingId") val deviceId: String,
	@SerializedName("heatingName") val name: String

)

@Entity(tableName = "localChanges", primaryKeys = ["deviceId"])
data class HeatingLocalChange(
	val deviceId: String,
	val timetable: MutableList<MutableList<Int>> = mutableListOf()
)