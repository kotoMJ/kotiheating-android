package cz.kotox.kotiheating.model.rest

import android.app.Application
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import cz.kotox.kotiheating.BuildConfig
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus
import cz.kotox.ktools.Resource
import cz.kotox.ktools.getRetrofit
import cz.kotox.ktools.inject
import cz.kotox.ktools.mapResource
import okhttp3.logging.HttpLoggingInterceptor

class HeatingStatusApi {

	val application by inject<Application>()

	private val headerRequestInterceptor by inject<HeaderRequestInterceptor>()

	val gson by inject<Gson>()

	val api: HeatingRouter
		get() {
			return getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson,
					customInterceptors = *arrayOf(headerRequestInterceptor)).create(HeatingRouter::class.java)
		}

	fun getHeatingStatus(heatingId: String): LiveData<Resource<HeatingDeviceStatus>> {
		return api.getHeatingStatusLive(heatingId).mapResource { it?.heatingDeviceStatus }
	}
}