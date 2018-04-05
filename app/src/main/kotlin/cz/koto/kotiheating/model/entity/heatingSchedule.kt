package cz.koto.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

enum class ScheduleType {
	DEVICE,
	REQUEST_LOCAL,
	REQUEST_REMOTE,
	UNKNOWN;

	override fun toString(): String {
		return name
	}

	companion object {
		fun fromString(value: String?) = value?.let { ScheduleType.values().find { it.name == value } ?: UNKNOWN }
				?: UNKNOWN
	}
}

data class HeatingScheduleResult(
		@SerializedName("result") val heatingSchedule: HeatingSchedule
)

@Entity(tableName = "heatingSchedule", primaryKeys = ["deviceId", "scheduleType"])
data class HeatingSchedule(
		@SerializedName("typeId") var scheduleType: ScheduleType,
		@SerializedName("deviceId") var deviceId: String,
		@SerializedName("timetable") var timetable: MutableList<MutableList<Float>>
) {

	constructor() : this(ScheduleType.UNKNOWN, "", mutableListOf())
}