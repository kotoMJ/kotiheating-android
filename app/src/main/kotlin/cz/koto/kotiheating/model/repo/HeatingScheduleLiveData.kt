package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.NetworkBoundResource
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

			override fun shouldFetch(dataFromCache: HeatingSchedule?) = (scheduleType != ScheduleType.REQUEST_LOCAL)

			override fun loadFromDb() = cache.getSchedule(deviceId, scheduleType)

			override fun createNetworkCall() = statusApi.getHeatingSchedule(scheduleType, deviceId)
		})
	}
}