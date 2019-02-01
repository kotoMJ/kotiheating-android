package cz.kotox.kotiheating.model.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus

@Dao
interface HeatingStatusDao {

	@Insert(onConflict = REPLACE)
	fun putHeatingStatus(heatingStatus: HeatingDeviceStatus)

	@Query("SELECT * FROM deviceStatus WHERE deviceId = :deviceId")
	fun getHeatingStatus(deviceId: String): LiveData<HeatingDeviceStatus>

	@Query("DELETE FROM deviceStatus WHERE deviceId = :deviceId")
	fun deleteHeatingStatus(deviceId: String): Int
}