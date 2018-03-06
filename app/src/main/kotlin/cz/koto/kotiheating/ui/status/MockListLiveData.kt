package cz.koto.kotiheating.ui.status

import android.arch.lifecycle.LiveData
import cz.koto.kotiheating.ktools.Resource
import cz.koto.kotiheating.ui.StatusItem

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