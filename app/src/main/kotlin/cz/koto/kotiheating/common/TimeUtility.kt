package cz.koto.kotiheating.common

import java.util.*

fun getCurrentHour(): Int {
	val rightNow = Calendar.getInstance()
	return rightNow.get(Calendar.HOUR_OF_DAY)
}