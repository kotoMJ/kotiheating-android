package cz.koto.kotiheating.model

import android.arch.lifecycle.LiveData
import common.log.logk
import cz.koto.kotiheating.model.db.HeatingStatusDao
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.ktools.inject

class HeatingCache {
	private val statusDao by inject<HeatingStatusDao>()

//	private val localChangeDao by inject<HeatingLocalChangeDao>()

	fun getStatus(deviceId: String): LiveData<HeatingDeviceStatus>? {
		val fromDb = statusDao.getHeatingStatus(deviceId)
		logk("Loading status from db: ${fromDb.value}")
		return fromDb
	}

	fun putStatus(heatingDeviceStatus: HeatingDeviceStatus) {
		logk("Storing heating device status: $heatingDeviceStatus")
		heatingDeviceStatus.let {
			val timetableLocal = it.timetableLocal
			if (timetableLocal == null || timetableLocal.isEmpty()) {
				it.timetableLocal = it.timetableServer
			}
		}
		statusDao.putHeatingStatus(heatingDeviceStatus)
	}

	fun removeSchedule(deviceId: String) {
		logk("Removing schedule for all device id's from db!")
		statusDao.deleteHeatingStatus(deviceId)
	}

//	fun getLocalChange(deviceId: String): LiveData<HeatingLocalChange>? {
//		val fromDb = localChangeDao.getLocalChange(deviceId)
//		logk("Reading local change from db: ${fromDb.value}")
//		return fromDb
//	}
//
//	fun putLocalChange(heatingLocalChange: HeatingLocalChange) {
//		logk("Saving local change: $heatingLocalChange")
//		localChangeDao.putLocalChange(heatingLocalChange)
//	}

}