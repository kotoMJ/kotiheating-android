package cz.koto.kotiheating.model.rest

import android.app.Application
import com.google.gson.Gson
import cz.koto.kotiheating.BuildConfig
import cz.koto.kotiheating.model.entity.HeatingScheduleSetRequest
import cz.koto.kotiheating.model.entity.HeatingStatusResult
import cz.koto.ktools.getRetrofit
import cz.koto.ktools.inject
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