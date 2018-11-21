package cz.koto.kotiheating.model.repo

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.model.rest.HeatingStatusApi
import cz.koto.ktools.NetworkBoundResource
import cz.koto.ktools.ResourceLiveData
import cz.koto.ktools.inject

class HeatingStatusLiveData : ResourceLiveData<HeatingDeviceStatus>() {

	private val statusApi by inject<HeatingStatusApi>()
	val cache by inject<HeatingCache>()

	fun refresh(deviceId: String) {
		setupCached(object : NetworkBoundResource.Callback<HeatingDeviceStatus> {
			override fun saveCallResult(item: HeatingDeviceStatus) {
				cache.putStatus(item)
			}

			override fun shouldFetch(dataFromCache: HeatingDeviceStatus?) = true

			override fun loadFromDb(): LiveData<HeatingDeviceStatus>? {
				return cache.getStatus(deviceId)
			}

			override fun createNetworkCall() = statusApi.getHeatingStatus(deviceId)
		})
	}
}