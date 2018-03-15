package cz.koto.kotiheating.model.rest

import android.app.Application
import com.google.gson.Gson
import cz.koto.kotiheating.BuildConfig
import cz.koto.kotiheating.model.entity.HeatingAuthResult
import cz.koto.ktools.getRetrofit
import cz.koto.ktools.inject
import okhttp3.logging.HttpLoggingInterceptor

class HeatingApi {

	val application by inject<Application>()

	val gson by inject<Gson>()

	val api = getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson).create(HeatingBaseRouter::class.java)

	suspend fun authorizeGoogleUser(idToken: String?): HeatingAuthResult? {
		if (idToken != null) {
			return api.authorizeGoogleUser(idToken).await()
		} else {
			throw IllegalStateException("idToken must not be null!")
		}

	}
}