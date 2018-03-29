package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.entity.MockHeatingScheduleLiveData
import cz.koto.kotiheating.model.entity.MockHeatingStatusLiveData
import cz.koto.kotiheating.model.entity.ScheduleType

class HeatingRepository {

	fun getStatus(deviceId: String?) = if (deviceId == null) {
		MockHeatingStatusLiveData("0")
	} else {
		HeatingStatusLiveData().apply { refresh(deviceId) }
	}


	fun getSchedule(deviceId: String?, scheduleType: ScheduleType) = if (deviceId == null) {
		MockHeatingScheduleLiveData(scheduleType, "0")
	} else {
		HeatingScheduleLiveData().apply { refresh(deviceId, scheduleType) }
	}
}