package cz.koto.kotiheating.model.entity

import com.google.gson.annotations.SerializedName

data class HeatingScheduleResult(
	@SerializedName("result") val heatingSchedule: HeatingSchedule
)

data class HeatingSchedule(
	@SerializedName("heatingId") var deviceId: String,
	@SerializedName("timetable") var timetable: MutableList<MutableList<Int>>
)

data class HeatingScheduleSetRequest(@SerializedName("timetable") var timetable: MutableList<MutableList<Int>>)