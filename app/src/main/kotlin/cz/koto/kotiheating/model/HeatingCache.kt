package cz.koto.kotiheating.model

import android.arch.lifecycle.LiveData
import common.log.logk
import cz.koto.kotiheating.model.db.HeatingScheduleDao
import cz.koto.kotiheating.model.db.HeatingStatusDao
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.ktools.inject

class HeatingCache {
	private val statusDao by inject<HeatingStatusDao>()
	private val scheduleDao by inject<HeatingScheduleDao>()

	fun getStatus(deviceId: String): LiveData<HeatingDeviceStatus>? {
		val fromDb = statusDao.getHeatingStatus(deviceId)
		logk("Reading status from db: ${fromDb.value}")
		return fromDb
	}

	fun putStatus(heatingDeviceStatus: HeatingDeviceStatus) {
		logk("Saving heating device status: $heatingDeviceStatus")
		statusDao.putHeatingStatus(heatingDeviceStatus)
	}

	fun getSchedule(deviceId: String): LiveData<HeatingSchedule>? {
		val fromDb = scheduleDao.getHeatingSchedule(deviceId)
		logk("Reading schedule for deviceId=[${deviceId}] with value ${fromDb.value}")
		return fromDb
	}

	fun getScheduleX(deviceId: String): HeatingSchedule? {
		val fromDb = scheduleDao.getHeatingScheduleX(deviceId)
		logk("Reading schedule for deviceId=[$deviceId] with value ${fromDb} ")

		return fromDb
	}

	fun putSchedule(heatingSchedule: HeatingSchedule) {
		logk("Saving heating schedule: $heatingSchedule")
		scheduleDao.putHeatingSchedule(heatingSchedule)
	}

	fun removeSchedule() {
		logk("Removing schedule for all device id's from db!")
		scheduleDao.deleteHeatingScheduleAllDevices()
	}

}