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

	fun hasLocalScheduleChanges(deviceId: String, scheduleType: ScheduleType): Boolean {

		val remote = cache.getScheduleX(deviceId, scheduleType, true)?.timetable ?: mutableListOf()
		val local = cache.getScheduleX(deviceId, scheduleType, false)?.timetable ?: mutableListOf()
		remote.forEachIndexed { index, remoteItem ->
			if (remoteItem.zip(local[index]) { a, b -> a.compareTo(b) != 0 }.contains(true)) return true
		}
		return false
	}

	suspend fun updateRemoteSchedule(schedule: HeatingSchedule) {

		val query = async(CommonPool) {
			// Async stuff
			cache.putSchedule(schedule, true)
		}

		query.await()
	}

	suspend fun updateLocalSchedule(schedule: HeatingSchedule) {

		val query = async(CommonPool) {
			// Async stuff
			cache.putSchedule(schedule, false)
		}

		query.await()
	}

	suspend fun removeSchedule(scheduleType: ScheduleType) {
		val query = async(CommonPool) {
			// Async stuff
			cache.removeSchedule(scheduleType)
		}

		query.await()
	}
}