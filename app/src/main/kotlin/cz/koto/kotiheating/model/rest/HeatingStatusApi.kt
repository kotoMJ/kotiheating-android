package cz.koto.kotiheating.model.rest

import android.app.Application
import android.arch.lifecycle.LiveData
import com.google.gson.Gson
import cz.koto.kotiheating.BuildConfig
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.ktools.Resource
import cz.koto.ktools.getRetrofit
import cz.koto.ktools.inject
import cz.koto.ktools.mapResource
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