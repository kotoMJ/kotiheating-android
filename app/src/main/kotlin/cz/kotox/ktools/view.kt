package cz.kotox.ktools

import android.view.View
import android.view.ViewTreeObserver
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun View.setVisibilityInivisble() {
	this.visibility = View.INVISIBLE
}

fun View.setVisibilityVisible() {
	this.visibility = View.VISIBLE
}

fun View.setVisibilityGone() {
	this.visibility = View.GONE
}


fun FloatingActionButton.showWithAnimation(delay: Int = 200) {
	visibility = View.INVISIBLE
	scaleX = 0.0f
	scaleY = 0.0f
	alpha = 0.0f
	viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
		override fun onPreDraw(): Boolean {
			viewTreeObserver.removeOnPreDrawListener(this)
			postDelayed({ show() }, delay.toLong())
			return true
		}
	})
}