package cz.koto.kotiheating.model.rest

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.model.entity.HeatingAuthResult
import cz.koto.kotiheating.model.entity.HeatingStatusResult
import cz.koto.ktools.Resource
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface HeatingRouter {

	@GET("kotinode/heating/status")
	fun getHeatingStatusLive(): LiveData<Resource<HeatingStatusResult>>


	@FormUrlEncoded
	@POST("kotinode/auth/google")
	fun authorizeGoogleUser(@Field("idToken") idToken: String): Deferred<HeatingAuthResult>

}
