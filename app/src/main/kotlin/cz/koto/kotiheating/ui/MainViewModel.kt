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

		statusDeviceList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.DEVICE), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})

		statusRequestRemoteList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST_REMOTE), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})

		statusRequestLocalList = DiffObservableLiveHeatingSchedule(heatingRepository.getSchedule(userRepository.heatingSet.firstOrNull(), ScheduleType.REQUEST_LOCAL), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}

	fun updateLocalList(newLocalSchedule: HeatingSchedule) {
		statusRequestLocalList.value?.data?.timetable = newLocalSchedule.timetable

		for (day in 0..6) {
			statusRequestLocalList.diffListMap[day]?.forEachIndexed { hour, item ->
				item.apply {
					item.temperature = newLocalSchedule.timetable[day][hour]
				}
			}
		}
		statusRequestLocalList.value = statusRequestLocalList.value

		newLocalSchedule.scheduleType = ScheduleType.REQUEST_LOCAL
		heatingRepository.updateSchedule(newLocalSchedule)
		newLocalSchedule.scheduleType = ScheduleType.REQUEST_REMOTE
		heatingRepository.updateSchedule(newLocalSchedule)
	}

	fun revertLocalChanges(day: ObservableInt) {
		statusRequestLocalList.diffListMap[day.get()]?.forEachIndexed { hour, item ->
			item.apply {
				statusDeviceList.diffListMap[day.get()]?.let { daySchedule ->
					item.temperature = daySchedule[hour].temperature
				}
			}
		}
		//hack to force listeners think that value has changed.
		statusRequestLocalList.value = statusRequestLocalList.value
	}

	fun setLocalTemperatureTo(day: Int, temp: Int) {
		statusRequestLocalList.diffListMap[day]?.forEachIndexed { _, item ->
			item.apply {
				item.temperature = temp
			}
		}
		//hack to force listeners think that value has changed.
		statusRequestLocalList.value = statusRequestLocalList.value
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
		logk(">>>${statusRequestLocalList.value}")
		logk(">>>${statusRequestLocalList.value?.data}")
		logk(">>>${statusRequestLocalList.value?.data?.timetable}")
		statusRequestLocalList.value?.data?.timetable?.let { timeTable ->
			val heatingSet = userRepository.heatingSet.firstOrNull()
					?: throw IllegalStateException("No heatingSet assigned to the user!")
			heatingSet.let { heatingId ->
				try {
					logk(">>>${heatingId}")
					return runBlocking(CommonPool) {
						scheduleApi.setHeatingSchedule(timeTable, heatingId)?.heatingSchedule
					}
				} catch (e: Throwable) {
					logk(">>>$e")
				}
			}
		}
		return null
	}

}

