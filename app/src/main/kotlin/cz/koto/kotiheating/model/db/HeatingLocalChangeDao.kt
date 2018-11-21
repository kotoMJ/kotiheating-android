//package cz.koto.kotiheating.model.db
//
//import android.arch.lifecycle.LiveData
//import android.arch.persistence.room.Dao
//import android.arch.persistence.room.Insert
//import android.arch.persistence.room.OnConflictStrategy.REPLACE
//import android.arch.persistence.room.Query
//import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
//import cz.koto.kotiheating.model.entity.HeatingLocalChange
//
//@Dao
//interface HeatingLocalChangeDao {
//
//	@Insert(onConflict = REPLACE)
//	fun putLocalChange(heatingChange: HeatingLocalChange)
//
//	@Query("SELECT * FROM deviceStatus WHERE deviceId = :deviceId")
//	fun getLocalChange(deviceId: String): LiveData<HeatingLocalChange>
//
//}