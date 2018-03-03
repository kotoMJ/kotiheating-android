package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.ViewTreeObserver
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ktools.DiffObservableListLiveData
import cz.koto.kotiheating.ktools.LifecycleAwareBindingRecyclerViewAdapter
import cz.koto.kotiheating.ktools.vmb
import cz.koto.kotiheating.ui.recycler.SwipeToLeftCallback
import cz.koto.kotiheating.ui.recycler.SwipeToRightCallback
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

		val swipeLeftHandler = object : SwipeToLeftCallback(this, vmb.viewModel) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateItem(viewHolder, increase = false)
			}
		}
		val itemTouchLeftHelper = ItemTouchHelper(swipeLeftHandler)
		itemTouchLeftHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)


		val swipeRightHandler = object : SwipeToRightCallback(this, vmb.viewModel) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateItem(viewHolder, increase = true)
			}
		}
		val itemTouchRightHelper = ItemTouchHelper(swipeRightHandler)
		itemTouchRightHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)
	}


	private fun updateItem(viewHolder: RecyclerView.ViewHolder, increase: Boolean) {
		val position = viewHolder.layoutPosition

		val updatedItem = vmb.binding.viewModel?.statusRequestList?.diffList?.get(position)

		updatedItem?.apply {
			if (increase) {
				temperature += 1
			} else {
				temperature -= 1
			}
		}

		val newList: ArrayList<StatusItem> = ArrayList(vmb.binding.viewModel?.statusRequestList?.diffList?.toList())

		updatedItem?.let {
			newList.set(position, it)
		}

		vmb.binding.viewModel?.statusRequestList?.diffList?.update(newList)

		vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
		vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
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


	var statusServerList: DiffObservableListLiveData<StatusItem>
	var statusRequestList: DiffObservableListLiveData<StatusItem>

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

