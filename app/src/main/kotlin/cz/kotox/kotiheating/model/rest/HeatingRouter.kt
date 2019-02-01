package cz.kotox.kotiheating.model.rest

import androidx.lifecycle.LiveData
import cz.kotox.kotiheating.model.entity.HeatingAuthResult
import cz.kotox.kotiheating.model.entity.HeatingScheduleSetRequest
import cz.kotox.kotiheating.model.entity.HeatingStatusResult
import cz.kotox.ktools.Resource
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HeatingRouter {

	@GET("kotinode/heating/status/{deviceId}")
	fun getHeatingStatusLive(@Path("deviceId") deviceId: String): LiveData<Resource<HeatingStatusResult>>

//	@GET("kotinode/heating/schedule/{deviceId}")
//	fun getHeatingScheduleLive(@Path("deviceId") deviceId: String): LiveData<Resource<HeatingScheduleResult>>

	@POST("kotinode/heating/schedule/{deviceId}")
	fun setHeatingSchedule(@Body heatingScheduleSetRequest: HeatingScheduleSetRequest, @Path("deviceId") deviceId: String): Deferred<HeatingStatusResult>

	@FormUrlEncoded
	@POST("kotinode/auth/google")
	fun authorizeGoogleUser(@Field("idToken") idToken: String): Deferred<HeatingAuthResult>

}
