package cz.kotox.kotiheating.model.repo

import android.arch.lifecycle.LiveData
import cz.kotox.kotiheating.common.areListsDifferent
import cz.kotox.kotiheating.model.HeatingCache
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus
import cz.kotox.kotiheating.model.rest.HeatingStatusApi
import cz.kotox.ktools.NetworkBoundResource
import cz.kotox.ktools.ResourceLiveData
import cz.kotox.ktools.inject

class HeatingStatusLiveData : ResourceLiveData<HeatingDeviceStatus>() {

	private val statusApi by inject<HeatingStatusApi>()
	val cache by inject<HeatingCache>()

	fun refresh(deviceId: String) {
		setupCached(object : NetworkBoundResource.Callback<HeatingDeviceStatus> {
			override fun saveCallResult(item: HeatingDeviceStatus) {
				cache.putStatus(item)
			}

			override fun shouldFetch(dataFromCache: HeatingDeviceStatus?): Boolean {
				val timeTableLocal = dataFromCache?.timetableLocal ?: return true
				return !areListsDifferent(localValues = timeTableLocal, serverValues = dataFromCache.timetableServer)
			}

			override fun loadFromDb(): LiveData<HeatingDeviceStatus>? {
				return cache.getStatus(deviceId)
			}

			override fun createNetworkCall() = statusApi.getHeatingStatus(deviceId)
		})
	}
}