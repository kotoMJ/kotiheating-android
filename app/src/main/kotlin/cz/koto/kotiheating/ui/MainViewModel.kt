package cz.koto.kotiheating.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.ObservableInt
import common.log.logk
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.kotiheating.model.repo.HeatingRepository
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.DiffObservableLiveHeatingSchedule
import cz.koto.ktools.inject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class MainViewModel : BaseViewModel() {

	var selectedDay = ObservableInt(/*Calendar.getInstance().get(Calendar.DAY_OF_WEEK)*/0)

	val userRepository by inject<UserRepository>()
	val heatingRepository by inject<HeatingRepository>()

	val itemBinding = ItemBinding.of<StatusItem>(BR.item, R.layout.item_heating)
			.bindExtra(BR.viewModel, this)


	var statusDeviceList: DiffObservableLiveHeatingSchedule<HeatingSchedule>
	var statusRequestRemoteList: DiffObservableLiveHeatingSchedule<HeatingSchedule>
	var statusRequestLocalList: DiffObservableLiveHeatingSchedule<HeatingSchedule>

	private val scheduleApi by inject<HeatingScheduleApi>()

	init {

		userRepository.checkGoogleAccounts()
		statusRequestLocalList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST_LOCAL), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
		statusDeviceList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.DEVICE), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})

		statusRequestRemoteList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST_REMOTE), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}

	fun refreshDataFromServer() {
		launch(UI) {
			heatingRepository.removeSchedule(ScheduleType.REQUEST_LOCAL)
		}
		statusDeviceList.connectSource(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.DEVICE))
		statusRequestRemoteList.connectSource(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST_REMOTE))
		statusRequestLocalList.connectSource(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST_LOCAL))
	}

	fun updateRequestListWithServerResponse(serverResponseSchedule: HeatingSchedule) {
		statusRequestLocalList.value?.data?.timetable = serverResponseSchedule.timetable
		statusRequestRemoteList.value?.data?.timetable = serverResponseSchedule.timetable

		for (day in 0..6) {
			statusRequestLocalList.diffListMap[day]?.forEachIndexed { hour, item ->
				item.apply {
					item.temperature = serverResponseSchedule.timetable[day][hour]
				}
			}
		}

		for (day in 0..6) {
			statusRequestRemoteList.diffListMap[day]?.forEachIndexed { hour, item ->
				item.apply {
					item.temperature = serverResponseSchedule.timetable[day][hour]
				}
			}
		}


		launch(UI) {
			serverResponseSchedule.scheduleType = ScheduleType.REQUEST_LOCAL
			heatingRepository.updateSchedule(serverResponseSchedule)
			serverResponseSchedule.scheduleType = ScheduleType.REQUEST_REMOTE
			heatingRepository.updateSchedule(serverResponseSchedule)
		}

	}

	fun revertLocalChanges(day: ObservableInt) {
//		statusRequestLocalList.diffListMap[day.get()]?.forEachIndexed { hour, item ->
//			item.apply {
//				statusDeviceList.diffListMap[day.get()]?.let { daySchedule ->
//					item.temperature = daySchedule[hour].temperature
//				}
//			}
//		}
//		//hack to force listeners think that value has changed.
//		statusRequestLocalList.value = statusRequestLocalList.value
//
//		launch(UI) {
//			statusRequestLocalList.value?.data?.let { heatingRepository.updateSchedule(it) }
//		}

		refreshDataFromServer()
		statusRequestLocalList.value = statusRequestRemoteList.value
		statusDeviceList.diffListMap[day.get()]?.forEachIndexed { hour, item ->
			item.apply {
				statusRequestLocalList.diffListMap[day.get()]?.let { daySchedule ->
					item.temperature = daySchedule[hour].temperature
				}
			}
		}
		launch(UI) {
			statusRequestLocalList.value?.data?.let {
				heatingRepository.updateSchedule(it)
			}

		}
	}


	fun decreaseLocalHourlyTemperatureTo(day: Int, hour: Int) {
		statusRequestLocalList.diffListMap.get(day)?.let { dayList ->
			val updatedItem = dayList[hour]
			val newTemperature = updatedItem.temperature - 10
			statusRequestLocalList.setHourlyTemperatureTo(day, hour, newTemperature)
		}

		launch(UI) {
			statusRequestLocalList.value?.data?.let {
				heatingRepository.updateSchedule(it)
			}
		}
	}

	fun increaseLocalHourlyTemperatureTo(day: Int, hour: Int) {
		statusRequestLocalList.diffListMap.get(day)?.let { dayList ->
			val updatedItem = dayList[hour]
			val newTemperature = updatedItem.temperature + 10
			statusRequestLocalList.setHourlyTemperatureTo(day, hour, newTemperature)
		}

		launch(UI) {
			statusRequestLocalList.value?.data?.let {
				heatingRepository.updateSchedule(it)
			}
		}
	}

	fun setLocalDailyTemperatureTo(day: Int, temp: Int) {
		statusRequestLocalList.setDailyTemperatureTo(day, temp)
		launch(UI) {
			statusRequestLocalList.value?.data?.let {
				heatingRepository.updateSchedule(it)
			}

		}
	}

	@SuppressLint("RestrictedApi")
	fun getSignInGoogleIntent(): Intent {
		return userRepository.googleSignInClient.signInIntent
	}

	fun handleSignInGoogleResult(signInGoogleResultIntent: Intent, credentialsHasChanged: () -> Unit) {
		userRepository.handleSignInResult(signInGoogleResultIntent, credentialsHasChanged)
	}


	fun signOutGoogleUser(credentialsHasChanged: () -> Unit) {
		userRepository.signOutGoogleUser(credentialsHasChanged)
	}

	fun isGoogleUserSignedIn(): Boolean {
		return userRepository.googleSignInAccount != null
	}

	fun sendRequestForSchedule(): HeatingSchedule? {
		logk("Sending schedule request timetable:${statusRequestLocalList.value?.data?.timetable}")
		statusRequestLocalList.value?.data?.timetable?.let { timeTable ->
			val heatingSet = userRepository.heatingSet.firstOrNull()
					?: throw IllegalStateException("No heatingSet assigned to the user!")
			heatingSet.let { heatingId ->
				try {
					logk("Sending schedule request for heatingId:${heatingId}")
					return runBlocking(CommonPool) {
						scheduleApi.setHeatingSchedule(timeTable, heatingId)?.heatingSchedule
					}
				} catch (e: Throwable) {
					logk("Unable to set new schedule! $e")
				}
			}
		}
		return null
	}

}

