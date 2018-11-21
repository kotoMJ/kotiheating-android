package cz.koto.kotiheating.model.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import cz.koto.kotiheating.model.entity.HeatingSchedule

@Dao
interface HeatingScheduleDao {

	@Insert(onConflict = REPLACE)
	fun putHeatingSchedule(heatingSchedule: HeatingSchedule)

	@Query("SELECT * FROM heatingSchedule")
	fun getHeatingScheduleAll(): LiveData<HeatingSchedule>

	@Query("SELECT * FROM heatingSchedule WHERE deviceId = :deviceId")
	fun getHeatingScheduleX(deviceId: String): HeatingSchedule

	@Query("SELECT * FROM heatingSchedule WHERE deviceId = :deviceId")
	fun getHeatingSchedule(deviceId: String): LiveData<HeatingSchedule>

	@Query("DELETE FROM heatingSchedule WHERE deviceId = :deviceId")
	fun deleteHeatingSchedule(deviceId: String): Int

	@Query("DELETE FROM heatingSchedule")
	fun deleteHeatingScheduleAllDevices(): Int
}