package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
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

	fun hasLocalScheduleChanges(deviceId: String): Boolean {

		val remote = cache.getStatus(deviceId)?.value?.timetableServer ?: mutableListOf()
		val local = cache.getStatus(deviceId)?.value?.timetableServer ?: mutableListOf()//TODO local
		remote.forEachIndexed { index, remoteItem ->
			if (remoteItem.zip(local[index]) { a, b -> a.compareTo(b) != 0 }.contains(true)) return true
		}
		return false
	}

	suspend fun updateStatus(schedule: HeatingDeviceStatus) {

		val query = GlobalScope.async(Dispatchers.Default) {
			// Async stuff
			cache.putStatus(schedule)
		}

		query.await()
	}

//	suspend fun updateLocalChange(localChange: HeatingLocalChange) {
//
//		val query = GlobalScope.async(Dispatchers.Default) {
//			// Async stuff
//			cache.putLocalChange(localChange)
//		}
//
//		query.await()
//	}

}