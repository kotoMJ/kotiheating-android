package cz.koto.kotiheating.model.entity

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.ui.StatusItem
import cz.koto.ktools.Resource
import java.util.*


fun getMockTimeTableMonday(): List<Float> = listOf(
		15f,
		15f,
		15f,
		15f,
		15f,
		15f,
		15f,
		15f,
		15f,
		15f,
		15f,
		16f,

		17f,
		18f,
		19f,
		20f,
		21f,
		22f,
		23f,
		23f,
		23f,
		22f,
		15f,
		15f)

fun getMockTimeTableDay(): List<Float> = listOf(
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,
		05f,

		10f,
		10f,
		10f,
		10f,
		10f,
		10f,
		10f,
		10f,
		10f,
		10f,
		10f,
		10f)

fun getMockTimeTableWeek(): List<List<Float>> = listOf(
		getMockTimeTableDay(),
		getMockTimeTableMonday(),
		getMockTimeTableDay(),
		getMockTimeTableDay(),
		getMockTimeTableDay(),
		getMockTimeTableDay(),
		getMockTimeTableDay(),
		getMockTimeTableDay()
)

class MockHeatingStatusLiveData(deviceId: String) : LiveData<Resource<HeatingDeviceStatus>>() {

	init {
		value = Resource(Resource.Status.SUCCESS,
				HeatingDeviceStatus(Date(), 22f, 2, "MO", 11, 8,
						getMockTimeTableWeek(), deviceId))
	}
}

class MockHeatingScheduleLiveData(scheduleType: ScheduleType, deviceId: String) : LiveData<Resource<HeatingSchedule>>() {

	init {
		value = Resource(Resource.Status.SUCCESS, HeatingSchedule(scheduleType, deviceId, getMockTimeTableWeek()))
	}

}

class MockListLiveData : LiveData<Resource<List<StatusItem>>>() {

	init {
		value = Resource(Resource.Status.SUCCESS, listOf(

				StatusItem(15f, 0),
				StatusItem(15f, 1),
				StatusItem(15f, 2),
				StatusItem(15f, 3),
				StatusItem(15f, 4),
				StatusItem(15f, 5),
				StatusItem(15f, 6),
				StatusItem(15f, 7),
				StatusItem(15f, 8),
				StatusItem(15f, 9),
				StatusItem(15f, 10),
				StatusItem(16f, 11),

				StatusItem(17f, 12),
				StatusItem(18f, 13),
				StatusItem(19f, 14),
				StatusItem(20f, 15),
				StatusItem(21f, 16),
				StatusItem(22f, 17),
				StatusItem(23f, 18),
				StatusItem(23f, 19),
				StatusItem(23f, 20),
				StatusItem(22f, 21),
				StatusItem(15f, 22),
				StatusItem(15f, 23)))
	}

}