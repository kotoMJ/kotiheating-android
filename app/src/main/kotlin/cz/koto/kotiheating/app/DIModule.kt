package cz.koto.kotiheating.app

import android.app.Application
import com.google.gson.Gson
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeaderRequestInterceptor
import cz.koto.kotiheating.model.rest.HeatingUserApi
import cz.koto.ktools.provideSingleton


object DIModule {
	fun initialize(application: Application) {
		provideSingleton { application }
		provideSingleton { Gson() }
		provideSingleton { HeatingUserApi() }
		provideSingleton { UserRepository() }
		provideSingleton {
			HeaderRequestInterceptor()
		}
	}
}


