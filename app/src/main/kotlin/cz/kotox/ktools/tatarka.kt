package cz.kotox.ktools

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.databinding.BindingAdapter
import android.databinding.ObservableInt
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cz.kotox.kotiheating.model.entity.HeatingDeviceStatus
import cz.kotox.kotiheating.ui.StatusItem
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

class DiffObservableLiveHeatingStatus<T : HeatingDeviceStatus>(liveData: LiveData<Resource<T>>, callback: DiffObservableList.Callback<StatusItem>) : MediatorLiveData<Resource<T>>() {

	val diffListMap = hashMapOf<Int, DiffObservableList<StatusItem>>()

	init {

		for (day in 0..6) {
			diffListMap[day] = DiffObservableList<StatusItem>(callback)
		}

		connectSource(liveData)

	}

	fun setHourlyTemperatureTo(setDay: Int, setHour: Int, setTemp: Int) {
		diffListMap[setDay]?.get(setHour)?.temperature = setTemp
		value?.data?.timetableLocal?.get(setDay)?.set(setHour, setTemp)
	}

	fun setDailyTemperatureTo(setDay: Int, setTemp: Int) {
		diffListMap[setDay]?.forEachIndexed { _, item ->
			item.apply {
				item.temperature = setTemp
			}
		}

		value?.data?.let {
			val timetableLocal = it.timetableLocal
			timetableLocal?.forEachIndexed { day, dayList ->
				timetableLocal[day] = mutableListOf(setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp, setTemp)
			}
			it.timetableLocal = timetableLocal
		}
	}

	fun connectSource(liveData: LiveData<Resource<T>>) {

		if (liveData.value?.data?.timetableLocal?.isNotEmpty() == true) {
			value = liveData.value
			liveData.value?.data.let {
				val timetableLocal = it?.timetableLocal
				timetableLocal?.forEachIndexed { day, dayList ->
					diffListMap[day]?.update(timetableLocal[day].mapIndexed { index, float ->
						StatusItem(float, index)
					})
				}
			}
		} else {
			value = Resource(Resource.Status.FAILURE, null)
		}

		addSource(liveData) {

			if (it?.data?.timetableLocal?.isNotEmpty() == true) {
				value = it

				it.data.let {
					val timetableLocal = it.timetableLocal
					timetableLocal?.forEachIndexed { day, dayList ->
						diffListMap[day]?.update(timetableLocal[day].mapIndexed { index, float ->
							StatusItem(float, index)
						})
					}

				}
			} else {
				value = Resource(Resource.Status.FAILURE, null)
			}
		}
	}
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter(value = ["liveDataItemBinding", "liveDataItems", "liveDataAdapter", "currentDay"], requireAll = false)
fun <T> setAdapterLiveData(recyclerView: RecyclerView,
	liveDataItemBinding: ItemBinding<StatusItem>,
	liveDataItems: DiffObservableLiveHeatingStatus<HeatingDeviceStatus>,
	presetAdapter: BindingRecyclerViewAdapter<StatusItem>?,
	currentDay: ObservableInt) {
	val oldAdapter = recyclerView.adapter as BindingRecyclerViewAdapter<StatusItem>?
	val adapter: BindingRecyclerViewAdapter<StatusItem>
	adapter = presetAdapter ?: (oldAdapter
		?: BindingRecyclerViewAdapter())
	if (oldAdapter !== adapter) {
		adapter.itemBinding = liveDataItemBinding
		adapter.setItems(liveDataItems.diffListMap[currentDay.get()])
		recyclerView.adapter = adapter
	}
}

class LifecycleAwareBindingRecyclerViewAdapter<T>(private val lifecycleOwner: LifecycleOwner) : BindingRecyclerViewAdapter<T>() {
	override fun onCreateBinding(inflater: LayoutInflater, @LayoutRes layoutId: Int, viewGroup: ViewGroup): ViewDataBinding {
		return super.onCreateBinding(inflater, layoutId, viewGroup).apply { setLifecycleOwner(lifecycleOwner) }
	}
}