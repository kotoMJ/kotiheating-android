package cz.koto.ktools

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.databinding.BindingAdapter
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.ui.StatusItem
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList

// TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle

//class DiffObservableListLiveData<T>(liveData: LiveData<Resource<List<T>>>, callback: DiffObservableList.Callback<T>) : MediatorLiveData<Resource<List<T>>>() {
//	val diffList = DiffObservableList<T>(callback)
//
//	init {
//		addSource(liveData, {
//			value = it
//			it?.data?.let { diffList.update(it) }
//		})
//	}
//}

class DiffObservableListLiveData<T>(liveData: LiveData<Resource<List<T>>>, callback: DiffObservableList.Callback<T>) : MediatorLiveData<Resource<List<T>>>() {
	val diffList = DiffObservableList<T>(callback)

	init {
		addSource(liveData, {
			value = it
			it?.data?.let { diffList.update(it) }
		})
	}
}

class DiffObservableLiveHeatingSchedule<T : HeatingSchedule>(liveData: LiveData<Resource<T>>, callback: DiffObservableList.Callback<StatusItem>) : MediatorLiveData<Resource<T>>() {

	val diffListMap = hashMapOf<Int, DiffObservableList<StatusItem>>()

	init {

		for (day in 0..6) {
			diffListMap[day] = DiffObservableList<StatusItem>(callback)
		}
		addSource(liveData, {

			if (it?.data?.timetable?.isNotEmpty() == true) {
				value = it

				it.data.let {
					it.timetable.forEachIndexed { day, dayList ->
						diffListMap.get(day)?.update(it.timetable[day].mapIndexed { index, float ->
							StatusItem(float, index)
						})
					}

				}
			} else {
				value = Resource(Resource.Status.FAILURE, null)
			}
		})
	}
}

//@Suppress("UNCHECKED_CAST")
//@BindingAdapter(value = ["liveDataItemBinding", "liveDataItems", "liveDataAdapter"], requireAll = false)
//fun <T> setAdapterLiveData(recyclerView: RecyclerView, liveDataItemBinding: ItemBinding<T>, liveDataItems: DiffObservableListLiveData<T>, presetAdapter: BindingRecyclerViewAdapter<T>?) {
//	val oldAdapter = recyclerView.adapter as BindingRecyclerViewAdapter<T>?
//	val adapter: BindingRecyclerViewAdapter<T>
//	adapter = presetAdapter ?: (oldAdapter
//			?: BindingRecyclerViewAdapter())
//	if (oldAdapter !== adapter) {
//		adapter.itemBinding = liveDataItemBinding
//		adapter.setItems(liveDataItems.diffList)
//		recyclerView.adapter = adapter
//	}
//}

@Suppress("UNCHECKED_CAST")
@BindingAdapter(value = ["liveDataItemBinding", "liveDataItems", "liveDataAdapter", "currentDay"], requireAll = false)
fun <T> setAdapterLiveData(recyclerView: RecyclerView,
						   liveDataItemBinding: ItemBinding<StatusItem>,
						   liveDataItems: DiffObservableLiveHeatingSchedule<HeatingSchedule>,
						   presetAdapter: BindingRecyclerViewAdapter<StatusItem>?,
						   currentDay: Int) {
	val oldAdapter = recyclerView.adapter as BindingRecyclerViewAdapter<StatusItem>?
	val adapter: BindingRecyclerViewAdapter<StatusItem>
	adapter = presetAdapter ?: (oldAdapter
			?: BindingRecyclerViewAdapter())
	if (oldAdapter !== adapter) {
		adapter.itemBinding = liveDataItemBinding
		adapter.setItems(liveDataItems.diffListMap[currentDay])
		recyclerView.adapter = adapter
	}
}

class LifecycleAwareBindingRecyclerViewAdapter<T>(private val lifecycleOwner: LifecycleOwner) : BindingRecyclerViewAdapter<T>() {
	override fun onCreateBinding(inflater: LayoutInflater, @LayoutRes layoutId: Int, viewGroup: ViewGroup): ViewDataBinding {
		return super.onCreateBinding(inflater, layoutId, viewGroup).apply { setLifecycleOwner(lifecycleOwner) }
	}
}