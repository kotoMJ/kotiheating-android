package cz.koto.kotiheating.model.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.model.entity.HeatingSchedule

@Database(entities = [(HeatingSchedule::class), (HeatingDeviceStatus::class)], version = 1)
@TypeConverters(Converters::class)
abstract class HeatingDatabase : RoomDatabase() {
	abstract fun scheduleDao(): HeatingScheduleDao
	abstract fun statusDao(): HeatingStatusDao
}