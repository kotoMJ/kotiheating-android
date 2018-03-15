package cz.koto.kotiheating.model.rest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import common.log.logk
import cz.koto.kotiheating.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

open class RetrofitClient() {

	val retrofit: Retrofit by lazy {
		buildRetrofit(30)
	}


	private fun buildRetrofit(timeoutInSecond: Long): Retrofit {
		val builder = Retrofit.Builder()
		builder.baseUrl(/*ScodashConfig.REST_BASE_URL*/"https://kotopeky.cz/api/")
		builder.client(buildClient(timeoutInSecond))
		builder.addConverterFactory(createConverterFactory())
		builder.addCallAdapterFactory(createCallAdapterFactory())
		return builder.build()
	}

	private fun buildClient(timeoutInSecond: Long): OkHttpClient {
		val builder = OkHttpClient.Builder()
		builder.connectTimeout(timeoutInSecond, TimeUnit.SECONDS)
		builder.readTimeout(timeoutInSecond, TimeUnit.SECONDS)
		builder.writeTimeout(timeoutInSecond, TimeUnit.SECONDS)
		builder.addNetworkInterceptor(createLoggingInterceptor())
		return builder.build()
	}


	private fun createLoggingInterceptor(): Interceptor {
		val logger = HttpLoggingInterceptor.Logger { logk(it) }
		val interceptor = HttpLoggingInterceptor(logger)
		interceptor.level = if (BuildConfig.LOGS) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
		return interceptor
	}


	fun createGson(): Gson {
		val builder = GsonBuilder()
		builder.registerTypeAdapter(Date::class.java, DateTypeDeserializer())
		return builder.create()
	}

	private fun createConverterFactory(): Converter.Factory {
		return GsonConverterFactory.create(createGson())
	}


	private fun createCallAdapterFactory(): CallAdapter.Factory {
		return RxJava2CallAdapterFactory.create()
	}
}
