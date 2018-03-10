package cz.koto.kotiheating.rest

import cz.koto.kotiheating.entity.HeatingStatusResult
import io.reactivex.Single
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface HeatingBaseRouter {

	@GET("/kotinode/heating/status")
	fun getHeatingStatus(): Single<Response<List<HeatingStatusResult>>>

	@GET("/kotinode/heating/status")
	fun getHeatingStatusLive(): Call<List<HeatingStatusResult>>

	@GET("/kotinode/heating/status")
	fun getHeatingStatusDeferred(): Deferred<Response<List<HeatingStatusResult>>>


}
