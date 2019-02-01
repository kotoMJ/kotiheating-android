package cz.kotox.ktools

import androidx.databinding.Observable

typealias ObservablePropertyCallback = Observable.OnPropertyChangedCallback

/**
 * Convenience extension function for using property change callback for observables.
 * When setting, calls the callback!
 * Uses typealias for callback
 *
 * @param propertyCallback callback when changed or set. When setting, [attr] is [null], otherwise it's attribute which was changed
 * @param whichProperty (optional) if specified, only if this property is changed, receiver [propertyCallback] is called
 * @return callback for setting to variable which is useful for later removing
 */
fun <T : Observable> T.observePropertyChange(whichProperty: Int? = null, propertyCallback: Int?.(observable: T) -> Unit): ObservablePropertyCallback {
	val callback = object : Observable.OnPropertyChangedCallback() {
		override fun onPropertyChanged(sender: Observable?, attr: Int) {
			if (whichProperty != null && attr != whichProperty) {
				return
			}
			propertyCallback(attr, this@observePropertyChange)
		}
	}

	propertyCallback(null, this) // when callback set, call this
	this.addOnPropertyChangedCallback(callback)
	return callback
}

/**
 * Multiple observables having same callback (meaning if any of them changes, callback is triggered)
 * @see [observePropertyChange]
 */
fun <T : Observable> observePropertyChange(vararg observables: T, whichProperty: Int? = null, propertyCallback: Int?.(observables: Array<out T>) -> Unit): Observable.OnPropertyChangedCallback {
	val callback = object : Observable.OnPropertyChangedCallback() {
		override fun onPropertyChanged(sender: Observable?, attr: Int) {
			if (whichProperty != null && attr != whichProperty) {
				return
			}
			propertyCallback(attr, observables)
		}
	}

	propertyCallback(null, observables)
	observables.forEach { it.addOnPropertyChangedCallback(callback) }
	return callback
}

/**
 * Convenience method for removing multiple observables
 */
fun ObservablePropertyCallback.removeFromObservable(vararg observable: Observable) {
	observable.forEach { it.removeOnPropertyChangedCallback(this) }
}