package cz.koto.kotiheating.ui

import android.content.Intent
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.model.entity.ScheduleType
import cz.koto.kotiheating.model.repo.HeatingRepository
import cz.koto.kotiheating.model.repo.UserRepository
import cz.koto.ktools.DiffObservableLiveHeatingSchedule
import cz.koto.ktools.inject
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class MainViewModel : BaseViewModel() {


	val userRepository by inject<UserRepository>()
	val heatingRepository by inject<HeatingRepository>()

	val itemBinding = ItemBinding.of<StatusItem>(BR.item, R.layout.item_heating)
			.bindExtra(BR.viewModel, this)


	var statusDeviceList: DiffObservableLiveHeatingSchedule
	var statusRequestRemoteList: DiffObservableLiveHeatingSchedule
	var statusRequestLocalList: DiffObservableLiveHeatingSchedule


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

	fun revertLocalChanges() {
//		statusRequestLocalList.diffList.forEachIndexed { index, item ->
//			item.apply {
//				item.temperature = statusDeviceList.diffList[index].temperature
//			}
//		}
	}

	fun setLocalTemperatureTo(temp: Float) {
//		statusRequestLocalList.diffList.forEachIndexed { _, item ->
//			item.apply {
//				item.temperature = temp
//			}
//		}
	}

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

}

