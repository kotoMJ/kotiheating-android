package cz.kotox.kotiheating.app

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import cz.kotox.kotiheating.model.HeatingCache
import cz.kotox.kotiheating.model.db.HeatingDatabase
import cz.kotox.kotiheating.model.repo.HeatingRepository
import cz.kotox.kotiheating.model.repo.UserRepository
import cz.kotox.kotiheating.model.rest.HeaderRequestInterceptor
import cz.kotox.kotiheating.model.rest.HeatingScheduleApi
import cz.kotox.kotiheating.model.rest.HeatingStatusApi
import cz.kotox.kotiheating.model.rest.HeatingUserApi
import cz.kotox.ktools.provideSingleton

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
		provideSingleton { HeatingStatusApi() }

		val database = Room.databaseBuilder(application, HeatingDatabase::class.java, "heating-database").allowMainThreadQueries().build()
		provideSingleton { database.statusDao() }
//		provideSingleton { database.localChangeDao() }
	}
}


