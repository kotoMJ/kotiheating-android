package cz.kotox.kotiheating.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus

@Database(entities = [(HeatingDeviceStatus::class)/*, (HeatingLocalChange::class)*/], version = 1)
@TypeConverters(Converters::class)
abstract class HeatingDatabase : RoomDatabase() {
	abstract fun statusDao(): HeatingStatusDao
	//abstract fun localChangeDao(): HeatingLocalChangeDao
}