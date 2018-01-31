package cz.koto.kotiheating.common

import android.app.Activity
import android.arch.lifecycle.*
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import cz.koto.kotiheating.BR
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


inline fun <reified VM : ViewModel, B : ViewDataBinding> vmb(layout: Int, noinline f: (Bundle?) -> VM = { _ -> VM::class.java.newInstance() }): ReadOnlyProperty<Any, ViewModelBinding<VM, B>> {
	return object : ReadOnlyProperty<Any, ViewModelBinding<VM, B>> {

		private var instance: ViewModelBinding<VM, B>? = null

		override fun getValue(thisRef: Any, property: KProperty<*>): ViewModelBinding<VM, B> {
			if (instance == null)
				instance = when (thisRef) {
					is Fragment -> ViewModelBinding(thisRef, layout, f, VM::class.java)
					is FragmentActivity -> ViewModelBinding(thisRef, layout, f, VM::class.java)
					else -> error("vmb delegate can be used only in Fragment or FragmentActivity")
				}
			return instance!!
		}
	}
}

//inline fun <reified VM: ViewModel, B: ViewDataBinding> FragmentActivity.vmb(layout: Int, noinline f: (Bundle?) -> VM = { _ -> VM::class.java.newInstance() }) =
//    ViewModelBinding<VM, B>(this, layout, f, VM::class.java)
//
//inline fun <reified VM: ViewModel, B: ViewDataBinding> Fragment.vmb(layout: Int, noinline f: (Bundle?) -> VM = { _ -> VM::class.java.newInstance() }) =
//    ViewModelBinding<VM, B>(this, layout, f, VM::class.java)


class ViewModelBinding<VM : ViewModel, out B : ViewDataBinding>(
		private val lifecycleOwner: LifecycleOwner,
		private val layout: Int,
		private val vmFactory: (Bundle?) -> VM,
		private val vmClass: Class<VM>) {

	private val activity get() = (lifecycleOwner as? FragmentActivity) ?: fragment?.activity
	private val fragment get() = lifecycleOwner as? Fragment

	val binding: B by lazy {
		DataBindingUtil.inflate<B>(activity?.layoutInflater, layout, null, false)
	}

	val viewModel: VM by lazy {
		fragment?.viewModel { vmFactory(fragment?.arguments) }
				?: activity?.viewModel { vmFactory(activity?.intent?.extras) }
				?: error("Something is bad")
	}

	init {
		require(lifecycleOwner is FragmentActivity || lifecycleOwner is Fragment) {
			"lifecycleOwner has to be FragmentActivity or Fragment"
		}

		lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
			@OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
			fun onCreate() {
				// setup binding variables
				binding.setVariable(BR.viewModel, viewModel)
//                binding.setVariable(BR.view, fragment ?: activity)
				(viewModel as? LifecycleReceiver)?.onLifecycleReady(lifecycleOwner)
				(lifecycleOwner as? Activity)?.setContentView(binding.root)
			}
		})
	}

	private fun Fragment.viewModel(f: () -> VM): VM {
		return ViewModelProviders.of(this, factory(f)).get(vmClass)
	}

	private fun FragmentActivity.viewModel(f: () -> VM): VM {
		return ViewModelProviders.of(this, factory(f)).get(vmClass)
	}


	@Suppress("UNCHECKED_CAST")
	private fun factory(f: () -> VM) = object : ViewModelProvider.NewInstanceFactory() {

		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return f() as T
		}
	}

}

interface LifecycleReceiver {
	fun onLifecycleReady(lifecycleOwner: LifecycleOwner) {}
}

// extension functions connecting LiveData with ObservableField
fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, observableField: ObservableField<T>) {
	this.observe(lifecycleOwner, android.arch.lifecycle.Observer { observableField.set(it) })
}

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner): ObservableField<T> {
	val observableField = ObservableField<T>()
	this.observe(lifecycleOwner, android.arch.lifecycle.Observer { observableField.set(it) })
	return observableField
}

fun <T> ObservableField<T>.observe(observer: (T) -> Unit) {
	this.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
		override fun onPropertyChanged(p0: Observable?, p1: Int) {
			observer(this@observe.get())
		}
	})
}

//
//
// single-event LiveData
class SingleLiveData<T> : MutableLiveData<T>() {
	private var pending = false

	override fun observe(owner: LifecycleOwner, observer: Observer<T>) {
		if (hasActiveObservers()) {
			Log.w("SingleLiveData", "Multiple observers registered but only one will be notified of changes.")
		}

		// Observe the internal MutableLiveData
		super.observe(owner, Observer {
			if (pending) {
				pending = false
				observer.onChanged(it)
			}
		})
	}

	override fun setValue(t: T?) {
		pending = true
		super.setValue(t)
	}

}