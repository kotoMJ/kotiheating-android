package cz.koto.kotiheating.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.ObservableInt
import common.log.logk
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.repo.HeatingRepository
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.DiffObservableLiveHeatingSchedule
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

	var statusRequestList: DiffObservableLiveHeatingSchedule<HeatingSchedule>

	private val scheduleApi by inject<HeatingScheduleApi>()

	init {

		userRepository.checkGoogleAccounts()
		statusRequestList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull()), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}

	fun refreshDataFromServer() {
		GlobalScope.launch(Dispatchers.Main) {
			heatingRepository.removeSchedule()
		}
		statusRequestList.connectSource(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull()))
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

		GlobalScope.launch(Dispatchers.Main) {
			heatingRepository.updateLocalSchedule(serverResponseSchedule)
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
				heatingRepository.updateLocalSchedule(it)
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
				heatingRepository.updateLocalSchedule(it)
			}
		}
	}

	fun setLocalDailyTemperatureTo(day: Int, temp: Int) {
		statusRequestList.setDailyTemperatureTo(day, temp)
		GlobalScope.launch(Dispatchers.Main) {
			statusRequestList.value?.data?.let {
				heatingRepository.updateLocalSchedule(it)
			}

		}
	}

	fun differLocalRequestFromRemote(): Boolean {
		val keySet = userRepository.heatingSet.firstOrNull()
		return if (keySet == null) {
			false
		} else {
			heatingRepository.hasLocalScheduleChanges(keySet)
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

	fun sendRequestForSchedule(): HeatingSchedule? {
		logk("Sending schedule request timetable:${statusRequestList.value?.data?.timetable}")
		statusRequestList.value?.data?.timetable?.let { timeTable ->
			val heatingSet = userRepository.heatingSet.firstOrNull()
					?: throw IllegalStateException("No heatingSet assigned to the user!")
			heatingSet.let { heatingId ->
				try {
					logk("Sending schedule request for heatingId:$heatingId")
					return runBlocking(Dispatchers.Default) {
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

