package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.PropertyChangeRegistry


open class BaseViewModel : ViewModel(), Observable {

	@Transient
	private var observableCallbacks: PropertyChangeRegistry? = null

	override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
		if (observableCallbacks != null) {
			observableCallbacks?.remove(callback);
		}
	}

	override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
		if (observableCallbacks == null) {
			observableCallbacks = PropertyChangeRegistry()
		}
		observableCallbacks?.add(callback)
	}

	@Synchronized
	fun notifyChange() {
		if (observableCallbacks != null) {
			observableCallbacks?.notifyCallbacks(this, 0, null)
		}
	}


	fun notifyPropertyChanged(fieldId: Int) {
		if (observableCallbacks != null) {
			observableCallbacks?.notifyCallbacks(this, fieldId, null)
		}
	}

}