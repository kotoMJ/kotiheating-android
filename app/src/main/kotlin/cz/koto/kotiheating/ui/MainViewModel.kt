package cz.koto.kotiheating.ui

import android.app.Application
import android.databinding.Bindable
import android.databinding.ObservableField
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.entity.HEATING_KEY
import cz.koto.kotiheating.entity.USER_KEY
import cz.koto.kotiheating.ktools.DiffObservableListLiveData
import cz.koto.kotiheating.ktools.inject
import cz.koto.kotiheating.ktools.sharedPrefs
import cz.koto.kotiheating.ktools.string
import cz.koto.kotiheating.ui.status.MockListLiveData
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class MainViewModel : BaseViewModel() {

	private val application by inject<Application>()

	val itemBinding = ItemBinding.of<StatusItem>(BR.item, R.layout.item_heating)
			.bindExtra(BR.viewModel, this)


	var statusDeviceList: DiffObservableListLiveData<StatusItem>
	var statusRequestRemoteList: DiffObservableListLiveData<StatusItem>
	var statusRequestLocalList: DiffObservableListLiveData<StatusItem>

	var googleSignInAccount: GoogleSignInAccount? = null
		@Bindable get
		set(value) {
			field = value
			notifyPropertyChanged(BR.googleSignInAccount)
		}

	var googleSignInAccountError: ObservableField<String> = ObservableField()


	val googleEmail: String
		@Bindable("googleSignInAccount")
		get() = googleSignInAccount?.email ?: "demo@profile.com"

	val googleName: String
		@Bindable("googleSignInAccount")
		get() = googleSignInAccount?.displayName ?: "Demo User"


	lateinit var googleSignInClient: GoogleSignInClient

	var heatingKey by application.sharedPrefs().string(HEATING_KEY)
	var userKey by application.sharedPrefs().string(USER_KEY)

	init {
		statusDeviceList = DiffObservableListLiveData(MockListLiveData(), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})

		statusRequestRemoteList = DiffObservableListLiveData(MockListLiveData(), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})

		statusRequestLocalList = DiffObservableListLiveData(MockListLiveData(), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}

	fun revertLocalChanges() {
		//TODO
//		val originalList: ArrayList<StatusItem> = ArrayList(statusRequestRemoteList.diffList.toList())
//		statusRequestLocalList.diffList.update(originalList)
	}

	fun setLocalTemperatureTo(temperature: Float) {
		//TODO
	}


}

