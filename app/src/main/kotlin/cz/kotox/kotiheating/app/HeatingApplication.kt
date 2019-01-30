package cz.kotox.kotiheating.app

import android.app.Application

class HeatingApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		DIModule.initialize(this)
	}
}