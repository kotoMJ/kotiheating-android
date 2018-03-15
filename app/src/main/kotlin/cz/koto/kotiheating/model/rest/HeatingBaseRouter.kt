package cz.koto.kotiheating.model.rest

import cz.koto.kotiheating.model.entity.HeatingAuthResult
import cz.koto.kotiheating.model.entity.HeatingStatusResult
import io.reactivex.Single
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface HeatingBaseRouter {

	@GET("kotinode/heating/status")
	fun getHeatingStatus(): Single<Response<List<HeatingStatusResult>>>

	@GET("kotinode/heating/status")
	fun getHeatingStatusLive(): Call<List<HeatingStatusResult>>

	@GET("kotinode/heating/status")
	fun getHeatingStatusDeferred(): Deferred<Response<List<HeatingStatusResult>>>


	@FormUrlEncoded
	@POST("kotinode/auth/google")
	fun authorizeGoogleUser(@Field("idToken") idToken: String): Deferred<HeatingAuthResult>

}
