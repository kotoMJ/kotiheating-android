package cz.kotox.kotiheating.model.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus

@Database(entities = [(HeatingDeviceStatus::class)/*, (HeatingLocalChange::class)*/], version = 1)
@TypeConverters(Converters::class)
abstract class HeatingDatabase : RoomDatabase() {
	abstract fun statusDao(): HeatingStatusDao
	//abstract fun localChangeDao(): HeatingLocalChangeDao
}