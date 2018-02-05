package cz.koto.kotiheating.ui.status

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.support.v4.content.res.ResourcesCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import common.log.log
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.ArcUtils


internal class CircleStatusView : View {

	enum class DrawAction {
		NONE,
		CLEAN_VIEW,
		ANIMATE_DYNAMIC
	}

	enum class SweepAngleDeltaDirection {
		NONE,
		UP,
		DOWN,
	}

	private var interpolator: Interpolator? = null

	private var strokeWidth: Float = 0f
	private var defaultStrokeWidth: Float = 0f
	private var animationDuration: Int = 0
	private var backCircleColor: Int = 0


	// Head and tail paints are used to properly draw arc ends, that is, the beginning shouldn't be rounded
	// but the end should. So we create a head of #headAngle degrees in order to achieve this behavior.
	private var circlePaint: Paint? = null
	private var currentAngle: Float = 0.toFloat()
	private var sweepAngleDelta: Float = 0f
	private var sweepAngleDeltaDirection: SweepAngleDeltaDirection = SweepAngleDeltaDirection.UP

	private var endAngle: Float = 0f
	private var animationStartTime: Long = 0

	private var radius: Float = 0f

	private var initialized: Boolean = false
	private var drawAction: DrawAction = DrawAction.NONE

	private val currentFrameAngle: Float
		get() {
			val now = System.currentTimeMillis()
			val pathGone = (now - animationStartTime).toFloat() / animationDuration
			val interpolatedPathGone = interpolator!!.getInterpolation(pathGone)

			if (pathGone < 1.0f) {
				currentAngle = endAngle * interpolatedPathGone
			} else {
				currentAngle = endAngle
			}
			return currentAngle
		}

	constructor(context: Context) : super(context) {
		interpolator = AccelerateDecelerateInterpolator()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

	fun init(attrs: TypedArray,
			 customRadius: Float,
			 customStrokeWidth: Float) {
		interpolator = AccelerateDecelerateInterpolator()
		radius = customRadius
		defaultStrokeWidth = customStrokeWidth
		readAttributesAndSetupFields(attrs)

		setupStatusGeneralCirclePaint()
		initialized = true
	}


	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		if (!initialized) {
			return
		}
		val cX = canvas.width / 2.0f //Width/2 gives the horizontal centre
		val cY = canvas.height / 2.0f //Height/2 gives the vertical centre
		val centerPoint = PointF(cX, cY)

		when (drawAction) {
			DrawAction.CLEAN_VIEW -> onDrawCleanView()
			DrawAction.ANIMATE_DYNAMIC -> onDrawAnimateDynamic(canvas, centerPoint)
			else -> log(">>> Unimplemented draw action ${drawAction}")
		}


	}

	private fun onDrawCleanView() {
		circlePaint?.alpha = 0
		drawAction = DrawAction.NONE
	}


	private fun onDrawAnimateDynamic(canvas: Canvas, centerPoint: PointF) {
		val sweepAngleDeltaSpeed = 5.5f
		val animationRounds = 3
		animationDuration = 4000

		val startAngle = 0f
		val endAngle = 360f * animationRounds

		val sweepBoundaryAngle = 90f + updateSweepAngleDelta(sweepAngleDeltaSpeed)
		this.endAngle = endAngle

		val arcsPointsOnCircle = 360
		val arcsOverlayPoints = true

		if (animationStartTime == 0.toLong()) {
			animationStartTime = System.currentTimeMillis()
		}

		/**
		 * START SHAPE (decreasing first 90, then invisible). Disappears as the dynamic one grows.
		 */
//		if (currentFrameAngle < sweepBoundaryAngle) {
//			val startShapeStartAngle = startAngle - sweepBoundaryAngle + currentFrameAngle
//			ArcUtils.drawArc(canvas, centerPoint, radius, startShapeStartAngle, sweepBoundaryAngle, circlePaint!!)//body
//		}

		//https://stackoverflow.com/questions/27850634/how-to-draw-an-arc-segment-with-different-fill-and-stroke-colors-for-each-side

		circlePaint?.let {
			it.color = Color.parseColor("#55E08F")
			ArcUtils.drawArc(canvas, centerPoint, radius, -90f, 90f, it)//body
			it.color = Color.parseColor("#3274ff")
			ArcUtils.drawArc(canvas, centerPoint, radius, 0f, 90f, it)//body
			it.color = Color.parseColor("#EE4F4F")
			ArcUtils.drawArc(canvas, centerPoint, radius, 90f, 90f, it)//body
			it.color = Color.parseColor("#90caf9")
			ArcUtils.drawArc(canvas, centerPoint, radius, 180f, 90f, it)//body
		}

		val scaledValues = scale() // Get the scaled values
		var sliceStartPoint = -90f

		var textPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = Color.parseColor("#313131")
			textSize = 24f//radius * 0.05f//0.66f
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}

		for (i in 0 until scaledValues.size) {
			sliceStartPoint += scaledValues[i] // Update starting point of the next slice
			val radius = 360f
			val x = (radius * Math.cos(sliceStartPoint * Math.PI / 180f)).toFloat() + width / 2 - 10
			val y = (radius * Math.sin(sliceStartPoint * Math.PI / 180f)).toFloat() + height / 2 - 20
			canvas.drawText("$i", x, y, textPaint)
		}

//		/**
//		 * DYNAMIC SHAPE
//		 */
//		val sweepAngleDynamic = if (currentFrameAngle < sweepBoundaryAngle) currentFrameAngle else sweepBoundaryAngle
//		val startAngleDynamic = if (currentFrameAngle > sweepBoundaryAngle) currentFrameAngle - sweepBoundaryAngle else startAngle
//		ArcUtils.drawArc(canvas, centerPoint, radius, startAngleDynamic, sweepAngleDynamic, circlePaint!!, arcsPointsOnCircle, arcsOverlayPoints)

		val inBounds = currentFrameAngle < endAngle
//		if (inBounds) {
//			invalidate()
//		} else {
//			drawAction = DrawAction.NONE
//		}
		drawAction = DrawAction.NONE
	}

	private val datapoints = arrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f)

	private fun getTotal(): Float {
		var total = 0f
		for (`val` in this.datapoints)
			total += `val`
		return total
	}

	private fun scale(): FloatArray {
		val scaledValues = FloatArray(this.datapoints.size)
		val total = getTotal() // Total all values supplied to the chart
		for (i in 0 until this.datapoints.size) {
			scaledValues[i] = this.datapoints[i] / total * 360 // Scale each value
		}
		return scaledValues
	}


	private fun resetSweepAngleDelta() {
		sweepAngleDelta = 0f
		sweepAngleDeltaDirection = SweepAngleDeltaDirection.UP
	}

	private fun updateSweepAngleDelta(speed: Float, upperBoundary: Int = 170, bottomBoundary: Int = 0): Float {

		if (sweepAngleDelta > upperBoundary) {
			sweepAngleDeltaDirection = SweepAngleDeltaDirection.DOWN
		}

		if (sweepAngleDelta < bottomBoundary) {
			sweepAngleDeltaDirection = SweepAngleDeltaDirection.NONE
		}

		when (sweepAngleDeltaDirection) {
			SweepAngleDeltaDirection.UP -> {
				if (currentFrameAngle > 120) {
					sweepAngleDelta += speed
				}
			}
			SweepAngleDeltaDirection.DOWN -> sweepAngleDelta -= speed
			else -> {
				sweepAngleDelta = 0f
			}
		}

		return sweepAngleDelta
	}

	fun animateDynamic() {
		resetSweepAngleDelta()
		drawAction = DrawAction.ANIMATE_DYNAMIC
		currentAngle = 0f
		animationStartTime = 0
		invalidate()
	}


	fun cleanView() {
		drawAction = DrawAction.CLEAN_VIEW
		invalidate()
	}

	private fun readAttributesAndSetupFields(attrs: TypedArray) {
		applyAttributes(attrs)
	}

	private fun applyAttributes(a: TypedArray) {
		strokeWidth = a.getDimension(R.styleable.ProgressCircleLayout_strokeWidth, defaultStrokeWidth)
	}


	private fun setupStatusGeneralCirclePaint() {
		circlePaint = Paint().apply {
			style = Paint.Style.STROKE
			strokeCap = Paint.Cap.BUTT
			strokeWidth = this@CircleStatusView.strokeWidth
		}
	}


	fun setInterpolator(i: Interpolator) {
		interpolator = i
	}

}

