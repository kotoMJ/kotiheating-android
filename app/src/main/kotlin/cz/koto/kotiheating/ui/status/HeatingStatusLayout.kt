package cz.koto.customview.view.circle.loader

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.FrameLayout
import cz.koto.kotiheating.R
import cz.koto.kotiheating.ui.status.CircleStatusView


class HeatingStatusLayout : FrameLayout {

	companion object {
		// Workable width is going to be 20 % less than total width
		// 0.4f comes from 80 % / 2, because the radius is half of the workable width
		private const val MAX_RADIUS_MULTIPLIER = 0.4f
		// 25 % of the max radius
		private const val CIRCLE_SEPARATION_FACTOR = 0.25f
		private const val CIRCLE_STROKE_WIDTH_FACTOR = 1.3f
	}

	private val circleViewIds: IntArray = intArrayOf(R.id.circleAm, R.id.circlePm)
	private val circleViews: MutableList<CircleStatusView> = mutableListOf()


	// Attributes from layout
	private lateinit var attrs: TypedArray

	var completeColor: Int? = null
	var uncompleteColor: Int? = null

	private var animItemsCount = 2


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
		View.inflate(context, R.layout.heating_status_layout, this)
	}

	fun showLayout(): Boolean {
		if (measuredWidth != 0) {
			calculateLayout(context, measuredWidth * MAX_RADIUS_MULTIPLIER)
			showViews()
			return true
		}
		return false
	}

	private fun calculateLayout(context: Context, maxRadius: Float) {
		cleanUpLayout()
		(0 until animItemsCount).mapTo(circleViews) { findViewById<CircleStatusView>(circleViewIds[it]) }
		val circleSeparation = maxRadius * CIRCLE_SEPARATION_FACTOR
		circleViews.forEachIndexed { index, orderStatusView ->
			if (index >= animItemsCount) return
			val radius = maxRadius - (circleSeparation * index)
			orderStatusView.init(attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR)

		}
	}

	private fun cleanUpLayout() {
		circleViews.clear()

		(0 until circleViewIds.size)
				.map { findViewById<CircleStatusView>(circleViewIds[it]) }
				.forEach { it.cleanView() }

	}

	fun setInterpolator(i: Interpolator) {
		circleViews.forEach { it.setInterpolator(i) }
	}

	private fun showViews() {
		circleViews.forEachIndexed { _, orderStatusView ->
			orderStatusView.showView()
		}
	}

}

