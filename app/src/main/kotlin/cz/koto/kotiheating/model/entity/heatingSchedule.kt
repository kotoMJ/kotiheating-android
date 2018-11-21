package cz.koto.kotiheating.model.entity

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

data class HeatingScheduleResult(
		@SerializedName("result") val heatingSchedule: HeatingSchedule
)

@Entity(tableName = "heatingSchedule", primaryKeys = ["deviceId", "remoteCopy"])
data class HeatingSchedule(
		@SerializedName("heatingId") var deviceId: String,
		@SerializedName("timetable") var timetable: MutableList<MutableList<Int>>,
		var remoteCopy: Boolean
) {

	constructor() : this("", mutableListOf(), false)
}

data class HeatingScheduleSetRequest(@SerializedName("timetable") var timetable: MutableList<MutableList<Int>>)