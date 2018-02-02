package com.legalzoom.kollaborate.base.ui.ordersview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.FrameLayout
import cz.koto.kotiheating.R
import cz.koto.kotiheating.ui.circle.loader.CircleLoaderView


class CircleLoaderLayout : FrameLayout {

	companion object {
		// Workable width is going to be 10 % less than total width
		// 0.45f comes from 90 % / 2, because the radius is half of the workable width
		private const val MAX_RADIUS_MULTIPLIER = 0.45f
		// 11.5 % of the max radius
		private const val CIRCLE_SEPARATION_FACTOR = 0.115f
		private const val CIRCLE_STROKE_WIDTH_FACTOR = 1.75f
	}

	private val orderViewsIds: IntArray = intArrayOf(R.id.circle1, R.id.circle2, R.id.circle3, R.id.circle4,
			R.id.circle5, R.id.circle6, R.id.circle7, R.id.circle8)
	private val orderViews: MutableList<CircleLoaderView> = mutableListOf()


	// Attributes from layout
	private lateinit var attrs: TypedArray

	var completeColor: Int? = null
	var uncompleteColor: Int? = null

	private var animItemsCount = 8


	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.attrs = context.theme.obtainStyledAttributes(attrs, R.styleable.ProgressCircleLayout, 0, 0)
		inflateViews()
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.attrs = context.theme.obtainStyledAttributes(attrs, R.styleable.ProgressCircleLayout, 0, 0)
		inflateViews()
	}


	@SuppressLint("SwitchIntDef")
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
		val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

		val width = measuredWidth
		val height = when (heightMode) {
			MeasureSpec.EXACTLY -> heightSize
			MeasureSpec.AT_MOST -> Math.min(measuredWidth, heightSize)
			else -> measuredWidth
		}

		val size = if (width < height) {
			height
		} else {
			height
		}

		for (i in 0 until childCount) {
			val child = getChildAt(i)
			val lp = child.layoutParams
			val widthMeasureMode = if (lp.width == FrameLayout.LayoutParams.WRAP_CONTENT) View.MeasureSpec.AT_MOST else View.MeasureSpec.EXACTLY
			val heightMeasureMode = if (lp.height == FrameLayout.LayoutParams.WRAP_CONTENT) View.MeasureSpec.AT_MOST else View.MeasureSpec.EXACTLY
			val widthMeasure = View.MeasureSpec.makeMeasureSpec(size, widthMeasureMode)
			val heightMeasure = View.MeasureSpec.makeMeasureSpec(size, heightMeasureMode)
			child.measure(widthMeasure, heightMeasure)
		}

		setMeasuredDimension(size, size)
	}

	private fun inflateViews() {
		View.inflate(context, R.layout.circle_loader_layout, this)
	}

	fun animateLayout(): Boolean {
		if (measuredWidth != 0) {
			calculateLayout(context, measuredWidth * MAX_RADIUS_MULTIPLIER)
			animateCalculatedLayout()
			return true
		}
		return false
	}

	private fun calculateLayout(context: Context, maxRadius: Float) {
		cleanUpLayout()
		(0 until animItemsCount).mapTo(orderViews) { findViewById<CircleLoaderView>(orderViewsIds[it]) }
		val circleSeparation = maxRadius * CIRCLE_SEPARATION_FACTOR
		orderViews.forEachIndexed { index, orderStatusView ->
			if (index >= animItemsCount) return
			val radius = maxRadius - (circleSeparation * index)
			orderStatusView.init(attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR)

		}
	}

	private fun cleanUpLayout() {
		orderViews.clear()

		(0 until orderViewsIds.size)
				.map { findViewById<CircleLoaderView>(orderViewsIds[it]) }
				.forEach { it.cleanView() }

	}

	fun setInterpolator(i: Interpolator) {
		orderViews.forEach { it.setInterpolator(i) }
	}

	private fun animateCalculatedLayout() {
		orderViews.forEachIndexed { _, orderStatusView ->
			orderStatusView.animateStatic()
		}
	}

}

