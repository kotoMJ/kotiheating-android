package cz.koto.kotiheating.model.rest

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.model.entity.*
import cz.koto.ktools.Resource
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.*

interface HeatingRouter {

	@GET("kotinode/heating/status/{deviceId}")
	fun getHeatingStatusLive(@Path("deviceId") deviceId: String): LiveData<Resource<HeatingStatusResult>>

	@GET("kotinode/heating/schedule/{deviceId}/{scheduleType}")
	fun getHeatingScheduleLive(@Path("deviceId") deviceId: String, @Path("scheduleType") scheduleType: ScheduleType): LiveData<Resource<HeatingScheduleResult>>

	@POST("kotinode/heating/schedule/{deviceId}")
	fun setHeatingSchedule(@Body heatingScheduleSetRequest: HeatingScheduleSetRequest, @Path("deviceId") deviceId: String): Deferred<HeatingScheduleResult>

	@FormUrlEncoded
	@POST("kotinode/auth/google")
	fun authorizeGoogleUser(@Field("idToken") idToken: String): Deferred<HeatingAuthResult>

}
