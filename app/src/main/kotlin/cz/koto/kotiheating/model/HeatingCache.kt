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
		logk("Reading status from db: ${fromDb.value}")
		return fromDb
	}

	fun putStatus(heatingDeviceStatus: HeatingDeviceStatus) {
		logk("Saving heating device status: $heatingDeviceStatus")
		statusDao.putHeatingStatus(heatingDeviceStatus)
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