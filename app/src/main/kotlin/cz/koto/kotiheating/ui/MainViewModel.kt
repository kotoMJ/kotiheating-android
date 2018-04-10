package cz.koto.kotiheating.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.ObservableInt
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.kotiheating.model.repo.HeatingRepository
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.kotiheating.model.rest.HeatingScheduleApi
import cz.koto.ktools.DiffObservableLiveHeatingSchedule
import cz.koto.ktools.inject
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
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

	fun revertLocalChanges(day: ObservableInt) {
		statusRequestLocalList.diffListMap[day.get()]?.forEachIndexed { index, item ->
			item.apply {
				statusDeviceList.diffListMap[day.get()]?.let { daySchedule ->
					item.temperature = daySchedule[index].temperature
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


	//TODO solve it!
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

	fun sendRequestForSchedule() {
		statusRequestLocalList.value?.data?.timetable?.let { timeTable ->
			userRepository.heatingSet.firstOrNull()?.let { heatingId ->
				launch(UI) {
					scheduleApi.setHeatingSchedule(timeTable, heatingId)
				}
			}
		}

	}

}

