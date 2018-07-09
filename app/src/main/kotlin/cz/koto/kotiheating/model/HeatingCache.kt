package cz.koto.kotiheating.model

import android.arch.lifecycle.LiveData
import common.log.logk
import cz.koto.kotiheating.model.db.HeatingScheduleDao
import cz.koto.kotiheating.model.db.HeatingStatusDao
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.ktools.inject


class HeatingCache {
	private val statusDao by inject<HeatingStatusDao>()
	private val scheduleDao by inject<HeatingScheduleDao>()


	fun getStatus(deviceId: String): LiveData<HeatingDeviceStatus> {
		val fromDb = statusDao.getHeatingStatus(deviceId)
		logk("Reading status from db: ${fromDb.value}")
		return fromDb
	}

	fun putStatus(heatingDeviceStatus: HeatingDeviceStatus) {
		logk("Saving heating device status: $heatingDeviceStatus")
		statusDao.putHeatingStatus(heatingDeviceStatus)
	}

	fun getSchedule(deviceId: String, scheduleType: ScheduleType): LiveData<HeatingSchedule> {
		val fromDb = scheduleDao.getHeatingSchedule(deviceId, scheduleType)
		logk("Reading schedule from db: ${fromDb.value}")
		return fromDb
	}

	fun putSchedule(heatingSchedule: HeatingSchedule) {
		logk("Saving heating schedule: $heatingSchedule")
		scheduleDao.putHeatingSchedule(heatingSchedule)
	}

	fun removeSchedule(scheduleType: ScheduleType) {
		logk("Removing schedule for all device id's and scheduleType=$scheduleType from db!")
		scheduleDao.deleteHeatingSchedule(scheduleType)
	}


}