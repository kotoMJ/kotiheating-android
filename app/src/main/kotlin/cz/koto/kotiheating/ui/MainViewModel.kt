package cz.koto.kotiheating.ui

import android.databinding.Bindable
import android.databinding.ObservableField
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import cz.koto.kotiheating.BR
import cz.koto.kotiheating.R
import cz.koto.kotiheating.ktools.DiffObservableListLiveData
import cz.koto.kotiheating.ui.status.MockListLiveData
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class MainViewModel : BaseViewModel() {

	val itemBinding = ItemBinding.of<StatusItem>(BR.item, R.layout.item_heating)
			.bindExtra(BR.viewModel, this)


	var statusServerList: DiffObservableListLiveData<StatusItem>
	var statusRequestList: DiffObservableListLiveData<StatusItem>

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


	init {
		statusServerList = DiffObservableListLiveData(MockListLiveData(), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})

		statusRequestList = DiffObservableListLiveData(MockListLiveData(), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = ((oldItem?.hour == newItem?.hour) && (oldItem?.temperature == newItem?.temperature))
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem?.hour == newItem?.hour
		})
	}


}

