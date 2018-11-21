package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.MockHeatingScheduleLiveData
import cz.koto.kotiheating.model.entity.MockHeatingStatusLiveData
import cz.koto.ktools.inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class HeatingRepository {

	val cache by inject<HeatingCache>()

	fun getStatus(deviceId: String?) = if (deviceId == null) {
		MockHeatingStatusLiveData("0")
	} else {
		HeatingStatusLiveData().apply { refresh(deviceId) }
	}

	fun getSchedule(deviceId: String?) = if (deviceId == null) {
		MockHeatingScheduleLiveData("0")
	} else {
		HeatingScheduleLiveData().apply { refresh(deviceId) }
	}

	fun hasLocalScheduleChanges(deviceId: String): Boolean {

		val remote = cache.getStatus(deviceId)?.value?.timetableServer ?: mutableListOf()
		val local = cache.getScheduleX(deviceId)?.timetable ?: mutableListOf()
		remote.forEachIndexed { index, remoteItem ->
			if (remoteItem.zip(local[index]) { a, b -> a.compareTo(b) != 0 }.contains(true)) return true
		}
		return false
	}

	suspend fun updateLocalSchedule(schedule: HeatingSchedule) {

		val query = GlobalScope.async(Dispatchers.Default) {
			// Async stuff
			cache.putSchedule(schedule)
		}

		query.await()
	}

	suspend fun removeSchedule() {
		val query = GlobalScope.async(Dispatchers.Default) {
			// Async stuff
			cache.removeSchedule()
		}

		query.await()
	}
}