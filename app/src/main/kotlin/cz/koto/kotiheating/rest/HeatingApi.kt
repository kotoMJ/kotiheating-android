package cz.koto.kotiheating.rest

import android.app.Application
import com.google.gson.Gson
import common.log.logk
import cz.koto.kotiheating.BuildConfig
import cz.koto.kotiheating.ktools.getRetrofit
import cz.koto.kotiheating.ktools.inject
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.logging.HttpLoggingInterceptor

class HeatingApi {

	val application by inject<Application>()

	val gson by inject<Gson>()

	val api = getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson).create(HeatingBaseRouter::class.java)

	fun authorizeGoogleUser(idToken: String?): Boolean {
		runBlocking {
			//			try {
			if (idToken != null) {
				val authResult = api.authorizeGoogleUser(idToken).await()
				logk("authResult=[$authResult]")
				return@runBlocking true
			} else {
				return@runBlocking false
			}
//			} catch (e: Throwable) {
//				logk("$e")
//				return@runBlocking false
//			}
		}

		return false
	}
}