package cz.koto.kotiheating.rest

import android.app.Application
import com.google.gson.Gson
import cz.koto.kotiheating.BuildConfig
import cz.koto.kotiheating.ktools.getRetrofit
import cz.koto.kotiheating.ktools.inject
import okhttp3.logging.HttpLoggingInterceptor

class HeatingApi {

	val application by inject<Application>()

	val gson by inject<Gson>()

	val api = getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson).create(HeatingBaseRouter::class.java)

	fun authorizeGoogleUser() {
		//return api.getHeatingStatus()
	}
}