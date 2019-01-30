package cz.kotox.kotiheating.model.repo

import cz.kotox.kotiheating.model.HeatingCache
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus
import cz.kotox.kotiheating.model.entity.MockHeatingStatusLiveData
import cz.kotox.ktools.inject
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

	suspend fun updateStatus(schedule: HeatingDeviceStatus) {

		val query = GlobalScope.async(Dispatchers.Default) {
			cache.putStatus(schedule)
		}

		query.await()
	}

	suspend fun removeStatus(deviceId: String) {
		val query = GlobalScope.async(Dispatchers.Default) {
			cache.removeSchedule(deviceId)
		}

		query.await()
	}

//	suspend fun updateLocalChange(localChange: HeatingLocalChange) {
//
//		val query = GlobalScope.async(Dispatchers.Default) {
//			cache.putLocalChange(localChange)
//		}
//
//		query.await()
//	}

}