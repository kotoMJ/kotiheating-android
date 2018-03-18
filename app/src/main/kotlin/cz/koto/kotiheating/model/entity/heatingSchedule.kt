package cz.koto.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

enum class ScheduleType {
	DEVICE,
	REQUEST_LOCAL,
	REQUEST_REMOTE
}

@Entity(tableName = "heatingSchedule", primaryKeys = ["deviceId", "scheduleType"])
data class HeatingSchedule(
		var scheduleType: ScheduleType,
		@SerializedName("timetable") val timetable: List<Array<Float>>,
		@SerializedName("deviceId") val deviceId: String
)