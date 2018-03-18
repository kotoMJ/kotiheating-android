package cz.koto.kotiheating.model.rest

import android.app.Application
import cz.koto.kotiheating.model.entity.HEATING_KEY
import cz.koto.kotiheating.model.entity.USER_KEY
import cz.koto.ktools.inject
import cz.koto.ktools.sharedPrefs
import cz.koto.ktools.string
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HeaderRequestInterceptor : Interceptor {

	private val application by inject<Application>()

	var heatingKey by application.sharedPrefs().string(HEATING_KEY)
	var userKey by application.sharedPrefs().string(USER_KEY)

	@Throws(IOException::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		val builder = chain.request().newBuilder()

		builder.addHeader("Accept-Charset", "utf-8")
		builder.addHeader("Content-Type", "application/json")

		if (!heatingKey.isNullOrBlank()) {
			builder.addHeader("key", heatingKey)
		}

		if (!userKey.isNullOrBlank()) {
			builder.addHeader("userKey", userKey)
		}

		val request = builder.build()
		return chain.proceed(request)
	}
}