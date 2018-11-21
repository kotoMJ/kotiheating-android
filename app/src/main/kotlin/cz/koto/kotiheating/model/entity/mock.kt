package cz.koto.kotiheating.model.entity

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.ui.StatusItem
import cz.koto.ktools.Resource
import java.util.Date

fun getMockTimeTableMonday(): MutableList<Int> = mutableListOf(
	150,
	150,
	150,
	150,
	150,
	150,
	150,
	150,
	150,
	150,
	150,
	160,

	170,
	180,
	190,
	200,
	210,
	220,
	230,
	230,
	230,
	220,
	150,
	150)

fun getMockTimeTableDay(): MutableList<Int> = mutableListOf(
	50,
	50,
	50,
	50,
	50,
	50,
	50,
	50,
	50,
	50,
	50,
	50,

	100,
	100,
	100,
	100,
	100,
	100,
	100,
	100,
	100,
	100,
	100,
	100)

fun getMockTimeTableWeek(): MutableList<MutableList<Int>> = mutableListOf(
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
			HeatingDeviceStatus(Date(), 220, 2, "MO", 11, 8,
				getMockTimeTableWeek(), getMockTimeTableWeek(), deviceId))
	}
}

class MockListLiveData : LiveData<Resource<List<StatusItem>>>() {

	init {
		value = Resource(Resource.Status.SUCCESS, listOf(

			StatusItem(150, 0),
			StatusItem(150, 1),
			StatusItem(150, 2),
			StatusItem(150, 3),
			StatusItem(150, 4),
			StatusItem(150, 5),
			StatusItem(150, 6),
			StatusItem(150, 7),
			StatusItem(150, 8),
			StatusItem(150, 9),
			StatusItem(150, 10),
			StatusItem(160, 11),

			StatusItem(170, 12),
			StatusItem(180, 13),
			StatusItem(190, 14),
			StatusItem(200, 15),
			StatusItem(210, 16),
			StatusItem(220, 17),
			StatusItem(230, 18),
			StatusItem(230, 19),
			StatusItem(230, 20),
			StatusItem(220, 21),
			StatusItem(150, 22),
			StatusItem(150, 23)))
	}

}