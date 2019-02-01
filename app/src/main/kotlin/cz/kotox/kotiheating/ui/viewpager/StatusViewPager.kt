package cz.kotox.kotiheating.ui.viewpager

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class StatusViewPager : androidx.viewpager.widget.ViewPager {

	constructor(context: Context) : super(context) {

	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

	}

	/**
	 * Compute height based on width of child.
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		//val size = computeHeightAsWidth(heightMeasureSpec)
		val size = computeMaximumWidthByChilds(widthMeasureSpec, heightMeasureSpec)
		setMeasuredDimension(size, size)
	}


	private fun computeMaximumWidthByChilds(widthMeasureSpec: Int, heightMeasureSpec: Int): Int {
		var height = 0
		for (i in 0 until childCount) {
			val child = getChildAt(i)
			child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
			val h = child.measuredHeight
			if (h > height) height = h
		}

		return height
	}

	private fun computeHeightAsWidth(heightMeasureSpec: Int): Int {
		val heightMode = MeasureSpec.getMode(heightMeasureSpec)
		val heightSize = MeasureSpec.getSize(heightMeasureSpec)

		val width = measuredWidth
		val height = when (heightMode) {
			MeasureSpec.EXACTLY -> heightSize
			MeasureSpec.AT_MOST -> Math.min(measuredWidth, heightSize)
			else -> measuredWidth
		}

		val size = if (width < height) {
			width
		} else {
			height
		}

		for (i in 0 until childCount) {
			val child = getChildAt(i)
			val lp = child.layoutParams
			val widthMeasureMode = if (lp.width == FrameLayout.LayoutParams.WRAP_CONTENT) MeasureSpec.AT_MOST else MeasureSpec.EXACTLY
			val heightMeasureMode = if (lp.height == FrameLayout.LayoutParams.WRAP_CONTENT) MeasureSpec.AT_MOST else MeasureSpec.EXACTLY
			val widthMeasure = MeasureSpec.makeMeasureSpec(size, widthMeasureMode)
			val heightMeasure = MeasureSpec.makeMeasureSpec(size, heightMeasureMode)
			child.measure(widthMeasure, heightMeasure)
		}
		return size
	}
}
