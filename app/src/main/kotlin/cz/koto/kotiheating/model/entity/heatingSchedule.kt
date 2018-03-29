package cz.koto.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

enum class ScheduleType {
	DEVICE,
	REQUEST_LOCAL,
	REQUEST_REMOTE
}

data class HeatingScheduleResult(
		@SerializedName("result") val heatingSchedule: HeatingSchedule
)

@Entity(tableName = "heatingSchedule", primaryKeys = ["deviceId", "scheduleType"])
data class HeatingSchedule(
		@SerializedName("typeId") var scheduleType: ScheduleType,
		@SerializedName("deviceId") val deviceId: String,
		@SerializedName("timetable") val timetable: List<List<Float>>
)