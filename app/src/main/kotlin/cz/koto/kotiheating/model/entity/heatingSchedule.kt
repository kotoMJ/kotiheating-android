package cz.koto.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

enum class ScheduleType {
	DEVICE,
	REQUEST,
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

@Entity(tableName = "heatingSchedule", primaryKeys = ["deviceId", "scheduleType", "remoteCopy"])
data class HeatingSchedule(
		@SerializedName("typeId") var scheduleType: ScheduleType,
		@SerializedName("heatingId") var deviceId: String,
		@SerializedName("timetable") var timetable: MutableList<MutableList<Int>>,
		var remoteCopy: Boolean
) {

	constructor() : this(ScheduleType.UNKNOWN, "", mutableListOf(), false)
}

data class HeatingScheduleSetRequest(@SerializedName("timetable") var timetable: MutableList<MutableList<Int>>)