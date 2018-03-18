package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.entity.ScheduleType

class HeatingRepository {
	fun getStatus(deviceId: String) = HeatingStatusLiveData().apply { refresh(deviceId) }
	fun getSchedule(deviceId: String, scheduleType: ScheduleType) = HeatingScheduleLiveData().apply { refresh(deviceId, scheduleType) }
}