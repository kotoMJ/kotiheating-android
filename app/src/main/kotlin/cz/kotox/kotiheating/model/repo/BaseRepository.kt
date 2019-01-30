package cz.kotox.kotiheating.model.repo

import android.databinding.Observable
import android.databinding.PropertyChangeRegistry


open class BaseRepository : Observable {

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