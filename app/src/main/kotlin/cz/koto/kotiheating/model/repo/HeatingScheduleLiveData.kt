package cz.koto.kotiheating.model.repo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.NetworkBoundResource
import cz.koto.ktools.Resource
import cz.koto.ktools.ResourceLiveData
import cz.koto.ktools.inject

class HeatingScheduleLiveData : ResourceLiveData<HeatingSchedule>() {

	private val statusApi by inject<HeatingScheduleApi>()
	val cache by inject<HeatingCache>()

	fun refresh(deviceId: String) {
		setupCached(object : NetworkBoundResource.Callback<HeatingSchedule> {
			override fun saveCallResult(item: HeatingSchedule) {
				cache.putSchedule(item)

			}

			override fun shouldFetch(dataFromCache: HeatingSchedule?): Boolean {
				return dataFromCache == null //Init local with remote
			}

			override fun loadFromDb(): LiveData<HeatingSchedule> {
				return cache.getSchedule(deviceId) ?: MutableLiveData<HeatingSchedule>()
			}

			override fun createNetworkCall(): LiveData<Resource<HeatingSchedule>> {
				return statusApi.getHeatingSchedule(deviceId)
			}
		})
	}

}