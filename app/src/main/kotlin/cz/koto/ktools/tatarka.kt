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

class DiffObservableListLiveData<T>(liveData: LiveData<Resource<List<T>>>, callback: DiffObservableList.Callback<T>) : MediatorLiveData<Resource<List<T>>>() {
	val diffList = DiffObservableList<T>(callback)

	init {
		addSource(liveData, {
			value = it
			it?.data?.let { diffList.update(it) }
		})
	}
}

class DiffObservableLiveHeatingSchedule(liveData: LiveData<Resource<HeatingSchedule>>, callback: DiffObservableList.Callback<StatusItem>) : MediatorLiveData<Resource<List<StatusItem>>>() {
	val diffList = callback

	init {
		addSource(liveData, {
			if (it?.data?.timetable?.isNotEmpty() == true) {
				value = Resource(Resource.Status.SUCCESS, it.data.timetable.mapIndexed { index, floats ->
					StatusItem(floats[index], index)
				})
			} else {
				value = Resource(Resource.Status.FAILURE, emptyList())
			}
		})
	}
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter(value = ["liveDataItemBinding", "liveDataItems", "liveDataAdapter"], requireAll = false)
fun <T> setAdapterLiveData(recyclerView: RecyclerView, liveDataItemBinding: ItemBinding<T>, liveDataItems: DiffObservableListLiveData<T>, presetAdapter: BindingRecyclerViewAdapter<T>?) {
	val oldAdapter = recyclerView.adapter as BindingRecyclerViewAdapter<T>?
	val adapter: BindingRecyclerViewAdapter<T>
	adapter = presetAdapter ?: (oldAdapter
			?: BindingRecyclerViewAdapter())
	if (oldAdapter !== adapter) {
		adapter.itemBinding = liveDataItemBinding
		adapter.setItems(liveDataItems.diffList)
		recyclerView.adapter = adapter
	}
}

class LifecycleAwareBindingRecyclerViewAdapter<T>(private val lifecycleOwner: LifecycleOwner) : BindingRecyclerViewAdapter<T>() {
	override fun onCreateBinding(inflater: LayoutInflater, @LayoutRes layoutId: Int, viewGroup: ViewGroup): ViewDataBinding {
		return super.onCreateBinding(inflater, layoutId, viewGroup).apply { setLifecycleOwner(lifecycleOwner) }
	}
}