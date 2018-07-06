package cz.koto.kotiheating.model.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.ScheduleType

@Dao
interface HeatingScheduleDao {

	@Insert(onConflict = REPLACE)
	fun putHeatingSchedule(heatingSchedule: HeatingSchedule)

	@Query("SELECT * FROM heatingSchedule WHERE deviceId = :deviceId AND scheduleType = :scheduleType")
	fun getHeatingSchedule(deviceId: String, scheduleType: ScheduleType): LiveData<HeatingSchedule>

	@Query("DELETE FROM heatingSchedule WHERE deviceId = :deviceId AND scheduleType = :scheduleType")
	fun deleteHeatingSchedule(deviceId: String, scheduleType: ScheduleType): Int

	@Query("DELETE FROM heatingSchedule WHERE scheduleType = :scheduleType")
	fun deleteHeatingSchedule(scheduleType: ScheduleType): Int
}