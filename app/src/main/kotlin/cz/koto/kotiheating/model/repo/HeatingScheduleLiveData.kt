package cz.koto.kotiheating.model.repo

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.NetworkBoundResource
import cz.koto.ktools.Resource
import cz.koto.ktools.ResourceLiveData
import cz.koto.ktools.inject


class HeatingScheduleLiveData : ResourceLiveData<HeatingSchedule>() {

	private val statusApi by inject<HeatingScheduleApi>()
	val cache by inject<HeatingCache>()

	fun refresh(deviceId: String, scheduleType: ScheduleType) {
		setupCached(object : NetworkBoundResource.Callback<HeatingSchedule> {
			override fun saveCallResult(item: HeatingSchedule) {
				item.scheduleType = scheduleType
				cache.putSchedule(item)
			}

			override fun shouldFetch(dataFromCache: HeatingSchedule?): Boolean {
				return when (scheduleType) {
					ScheduleType.REQUEST -> return dataFromCache == null //Init local with remote
					else -> true
				}
			}

			override fun loadFromDb(): LiveData<HeatingSchedule> {
				return cache.getSchedule(deviceId, scheduleType)
			}

			override fun createNetworkCall(): LiveData<Resource<HeatingSchedule>> {
				return statusApi.getHeatingSchedule(scheduleType, deviceId)
			}
		})
	}

}