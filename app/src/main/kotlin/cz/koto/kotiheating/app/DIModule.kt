package cz.koto.kotiheating.app

import android.app.Application
import com.google.gson.Gson
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeatingApi
import cz.koto.ktools.provideSingleton


object DIModule {
	fun initialize(application: Application) {
		provideSingleton { application }
		provideSingleton { Gson() }
		provideSingleton { HeatingApi() }
		provideSingleton { UserRepository() }
	}
}


