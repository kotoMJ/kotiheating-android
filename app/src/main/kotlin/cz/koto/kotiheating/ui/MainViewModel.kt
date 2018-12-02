package cz.koto.kotiheating.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.ObservableInt
import common.log.logk
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.areListsDifferent
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.model.repo.HeatingRepository
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.DiffObservableLiveHeatingStatus
import cz.koto.ktools.inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class MainViewModel : BaseViewModel() {

	var selectedDay = ObservableInt(/*Calendar.getInstance().get(Calendar.DAY_OF_WEEK)*/0)

	val userRepository by inject<UserRepository>()
	val heatingRepository by inject<HeatingRepository>()

	val itemBinding = ItemBinding.of<StatusItem>(BR.item, R.layout.item_heating)
		.bindExtra(BR.viewModel, this)

	var statusRequestList: DiffObservableLiveHeatingStatus<HeatingDeviceStatus>

	private val scheduleApi by inject<HeatingScheduleApi>()

	init {

		userRepository.checkGoogleAccounts()



		statusRequestList = DiffObservableLiveHeatingStatus(heatingRepository.getStatus(userRepository.heatingSet.firstOrNull()), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}

	fun refreshDataFromServer() {

//		GlobalScope.launch(Dispatchers.Main) {
//			statusRequestList.value?.data?.let {
//				heatingRepository.removeStatus(it.deviceId)
//			}
//		}

		statusRequestList.connectSource(heatingRepository.getStatus(userRepository.heatingSet.firstOrNull()))
	}

	fun updateRequestListWithServerResponse(serverResponseSchedule: HeatingDeviceStatus) {

		for (day in 0..6) {
			statusRequestList.diffListMap[day]?.forEachIndexed { hour, item ->
				item.apply {
					item.temperature = serverResponseSchedule.timetableServer[day][hour]
				}
			}
		}

		statusRequestList.value?.data?.timetableLocal = statusRequestList.value?.data?.timetableServer

		GlobalScope.launch(Dispatchers.Main) {
			heatingRepository.updateStatus(serverResponseSchedule)
		}

	}

	fun revertLocalChanges() {
		refreshDataFromServer()
	}

	fun decreaseLocalHourlyTemperatureTo(day: Int, hour: Int) {
		statusRequestList.diffListMap.get(day)?.let { dayList ->
			val updatedItem = dayList[hour]
			val newTemperature = updatedItem.temperature - 10
			statusRequestList.setHourlyTemperatureTo(day, hour, newTemperature)
		}

		GlobalScope.launch(Dispatchers.Main) {
			statusRequestList.value?.data?.let {
				heatingRepository.updateStatus(it)
			}
		}
	}

	fun increaseLocalHourlyTemperatureTo(day: Int, hour: Int) {
		statusRequestList.diffListMap.get(day)?.let { dayList ->
			val updatedItem = dayList[hour]
			val newTemperature = updatedItem.temperature + 10
			statusRequestList.setHourlyTemperatureTo(day, hour, newTemperature)
		}

		GlobalScope.launch(Dispatchers.Main) {
			statusRequestList.value?.data?.let {
				heatingRepository.updateStatus(it)
			}
		}
	}

	fun setLocalDailyTemperatureTo(day: Int, temp: Int) {
		statusRequestList.setDailyTemperatureTo(day, temp)
		GlobalScope.launch(Dispatchers.Main) {
			statusRequestList.value?.data?.let {
				heatingRepository.updateStatus(it)
			}

		}
	}

	fun differLocalRequestFromRemote(): Boolean {
		val status = statusRequestList.value?.data
		val timetableLocal = status?.timetableLocal
		return if (timetableLocal == null) {
			false
		} else {
			areListsDifferent(localValues = timetableLocal, serverValues = status.timetableServer)
		}
	}

	@SuppressLint("RestrictedApi")
	fun getSignInGoogleIntent(): Intent {
		return userRepository.googleSignInClient.signInIntent
	}

	fun handleSignInGoogleResult(signInGoogleResultIntent: Intent?, credentialsHasChanged: () -> Unit) {
		userRepository.handleSignInResult(signInGoogleResultIntent, credentialsHasChanged)
	}

	fun signOutGoogleUser(credentialsHasChanged: () -> Unit) {
		userRepository.signOutGoogleUser(credentialsHasChanged)
	}

	fun isGoogleUserSignedIn(): Boolean {
		return userRepository.googleSignInAccount != null
	}

	fun sendRequestForSchedule(): HeatingDeviceStatus? {
		logk("Sending schedule request timetable:${statusRequestList.value?.data?.timetableLocal}")
		statusRequestList.value?.data?.timetableLocal?.let { timeTable ->
			val heatingSet = userRepository.heatingSet.firstOrNull()
				?: throw IllegalStateException("No heatingSet assigned to the user!")
			heatingSet.let { heatingId ->
				try {
					logk("Sending schedule request for heatingId:$heatingId")
					return runBlocking(Dispatchers.Default) {
						scheduleApi.setHeatingSchedule(timeTable, heatingId)?.heatingDeviceStatus
					}
				} catch (e: Throwable) {
					logk("Unable to set new schedule! $e")
				}
			}
		}
		return null
	}

}

