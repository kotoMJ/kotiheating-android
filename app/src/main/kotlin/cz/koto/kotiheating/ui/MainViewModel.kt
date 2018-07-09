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
	var statusRequestList: DiffObservableLiveHeatingSchedule<HeatingSchedule>

	private val scheduleApi by inject<HeatingScheduleApi>()

	init {

		userRepository.checkGoogleAccounts()
		statusRequestList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
		statusDeviceList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.DEVICE), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}

	fun refreshDataFromServer() {
		launch(UI) {
			heatingRepository.removeSchedule(ScheduleType.REQUEST)
		}
		statusDeviceList.connectSource(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.DEVICE))
		statusRequestList.connectSource(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST))
	}

	fun updateRequestListWithServerResponse(serverResponseSchedule: HeatingSchedule) {
		statusRequestList.value?.data?.timetable = serverResponseSchedule.timetable

		for (day in 0..6) {
			statusRequestList.diffListMap[day]?.forEachIndexed { hour, item ->
				item.apply {
					item.temperature = serverResponseSchedule.timetable[day][hour]
				}
			}
		}

		launch(UI) {
			serverResponseSchedule.scheduleType = ScheduleType.REQUEST
			heatingRepository.updateSchedule(serverResponseSchedule)
		}

	}

	fun revertLocalChanges(day: ObservableInt) {
		refreshDataFromServer()
//		statusRequestList.value = statusRequestList.value
//		statusDeviceList.diffListMap[day.get()]?.forEachIndexed { hour, item ->
//			item.apply {
//				statusRequestList.diffListMap[day.get()]?.let { daySchedule ->
//					item.temperature = daySchedule[hour].temperature
//				}
//			}
//		}
//		launch(UI) {
//			statusRequestList.value?.data?.let {
//				heatingRepository.updateSchedule(it)
//			}
//
//		}
	}


	fun decreaseLocalHourlyTemperatureTo(day: Int, hour: Int) {
		statusRequestList.diffListMap.get(day)?.let { dayList ->
			val updatedItem = dayList[hour]
			val newTemperature = updatedItem.temperature - 10
			statusRequestList.setHourlyTemperatureTo(day, hour, newTemperature)
		}

		launch(UI) {
			statusRequestList.value?.data?.let {
				heatingRepository.updateSchedule(it)
			}
		}
	}

	fun increaseLocalHourlyTemperatureTo(day: Int, hour: Int) {
		statusRequestList.diffListMap.get(day)?.let { dayList ->
			val updatedItem = dayList[hour]
			val newTemperature = updatedItem.temperature + 10
			statusRequestList.setHourlyTemperatureTo(day, hour, newTemperature)
		}

		launch(UI) {
			statusRequestList.value?.data?.let {
				heatingRepository.updateSchedule(it)
			}
		}
	}

	fun setLocalDailyTemperatureTo(day: Int, temp: Int) {
		statusRequestList.setDailyTemperatureTo(day, temp)
		launch(UI) {
			statusRequestList.value?.data?.let {
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
		logk("Sending schedule request timetable:${statusRequestList.value?.data?.timetable}")
		statusRequestList.value?.data?.timetable?.let { timeTable ->
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

