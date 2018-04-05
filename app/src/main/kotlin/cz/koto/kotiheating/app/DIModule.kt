package cz.koto.kotiheating.app

import android.app.Application
import android.arch.persistence.room.Room
import com.google.gson.Gson
import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.db.HeatingDatabase
import cz.koto.kotiheating.model.repo.HeatingRepository
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeaderRequestInterceptor
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.kotiheating.model.rest.HeatingUserApi
import cz.koto.ktools.provideSingleton


object DIModule {
	fun initialize(application: Application) {
		provideSingleton { application }
		provideSingleton { Gson() }
		provideSingleton { HeatingCache() }
		provideSingleton { HeatingUserApi() }
		provideSingleton { UserRepository() }
		provideSingleton { HeatingRepository() }
		provideSingleton { HeaderRequestInterceptor() }
		provideSingleton { HeatingScheduleApi() }

		val database = Room.databaseBuilder(application, HeatingDatabase::class.java, "heating-database").build()
		provideSingleton { database.scheduleDao() }
		provideSingleton { database.statusDao() }
	}
}


