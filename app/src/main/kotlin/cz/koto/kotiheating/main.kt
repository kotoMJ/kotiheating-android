package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import cz.koto.kotiheating.ktools.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ktools.DiffObservableListLiveData
import cz.koto.kotiheating.ktools.LifecycleAwareBindingRecyclerViewAdapter
import cz.koto.kotiheating.ui.status.MockListLiveData
import cz.koto.kotiheating.ui.status.StatusItem
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class MainActivity : AppCompatActivity(), MainView {

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		vmb.binding.circleProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.circleProgress.showLayout()) {
					vmb.binding.circleProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})
	}

	override fun reloadStatus() {
		vmb.binding.circleProgress.showLayout()
	}

	override val lifecycleAwareAdapter = LifecycleAwareBindingRecyclerViewAdapter<StatusItem>(this)
}

interface MainView {
	fun reloadStatus()
	val lifecycleAwareAdapter: LifecycleAwareBindingRecyclerViewAdapter<StatusItem> // TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle

}


class MainViewModel : ViewModel() {

	val itemBinding = ItemBinding.of<StatusItem>(BR.item, R.layout.item_heating)
			.bindExtra(BR.viewModel, this)


	var statusList: DiffObservableListLiveData<StatusItem>

	init {
		statusList = DiffObservableListLiveData(MockListLiveData(), object : DiffObservableList.Callback<StatusItem> {
			override fun areContentsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem == newItem
			override fun areItemsTheSame(oldItem: StatusItem?, newItem: StatusItem?) = oldItem == newItem
		})
	}

}

