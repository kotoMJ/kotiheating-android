package cz.kotox.kotiheating.model.rest

import cz.kotox.kotiheating.model.repo.UserRepository
import cz.kotox.ktools.inject
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HeaderRequestInterceptor : Interceptor {

	private val userRepository by inject<UserRepository>()

	@Throws(IOException::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		val builder = chain.request().newBuilder()

		builder.addHeader("Accept-Charset", "utf-8")
		builder.addHeader("Content-Type", "application/json")

		if (userRepository.userKey.isNotBlank()) {
			builder.addHeader("userKey", userRepository.userKey)
		}

		val request = builder.build()
		return chain.proceed(request)
	}
}