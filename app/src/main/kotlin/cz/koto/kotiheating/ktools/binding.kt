package cz.koto.kotiheating.ktools

import android.databinding.BindingConversion
import android.view.View

/**
 * Conversion for Visibility - we can pass boolean as parameter in visible property
 */
@BindingConversion
fun convertBooleanToVisibility(visible: Boolean): Int {
	return if (visible) View.VISIBLE else View.GONE
}