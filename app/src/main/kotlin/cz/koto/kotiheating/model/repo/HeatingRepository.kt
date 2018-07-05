package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.MockHeatingScheduleLiveData
import cz.koto.kotiheating.model.entity.MockHeatingStatusLiveData
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.ktools.inject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

class HeatingRepository {

	val cache by inject<HeatingCache>()

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

	suspend fun updateSchedule(schedule: HeatingSchedule) {

		val query = async(CommonPool) {
			// Async stuff
			cache.putSchedule(schedule)
		}

		query.await()
	}
}