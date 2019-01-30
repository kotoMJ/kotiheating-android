package cz.kotox.kotiheating.common

import java.util.Calendar

fun getCurrentHour(): Int {
	val rightNow = Calendar.getInstance()
	return rightNow.get(Calendar.HOUR_OF_DAY)
}