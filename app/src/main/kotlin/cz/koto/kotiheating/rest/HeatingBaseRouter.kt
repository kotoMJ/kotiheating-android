package cz.koto.kotiheating.rest

import cz.koto.kotiheating.entity.HeatingStatusResult
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HeatingBaseRouter {

	@GET("/kotinode/heating/status")
	fun getHeatingStatus(@Path("user_id") userId: Int): Single<Response<List<HeatingStatusResult>>>

	@GET("/kotinode/heating/status")
	fun getHeatingStatusLive(@Path("user_id") userId: Int): Call<List<HeatingStatusResult>>
}
