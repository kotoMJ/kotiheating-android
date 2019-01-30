package cz.kotox.kotiheating.model.rest

import android.app.Application
import com.google.gson.Gson
import cz.kotox.kotiheating.BuildConfig
import cz.kotox.kotiheating.model.entity.HeatingScheduleSetRequest
import cz.kotox.kotiheating.model.entity.HeatingStatusResult
import cz.kotox.ktools.getRetrofit
import cz.kotox.ktools.inject
import okhttp3.logging.HttpLoggingInterceptor

class HeatingScheduleApi {

	val application by inject<Application>()

	private val headerRequestInterceptor by inject<HeaderRequestInterceptor>()

	val gson by inject<Gson>()

	val api: HeatingRouter
		get() {
			return getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson,
				customInterceptors = *arrayOf(headerRequestInterceptor)).create(HeatingRouter::class.java)
		}

	suspend fun setHeatingSchedule(timeTable: MutableList<MutableList<Int>>, deviceId: String): HeatingStatusResult? {
		return api.setHeatingSchedule(HeatingScheduleSetRequest(timeTable), deviceId).await()
	}
}