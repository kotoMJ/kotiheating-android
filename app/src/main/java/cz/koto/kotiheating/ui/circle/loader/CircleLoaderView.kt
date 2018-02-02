package cz.koto.kotiheating.ui.circle.loader

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import common.log.log
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.ArcUtils


internal class CircleLoaderView : View {

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

		setupPaint()
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


	/**
	 *	Dynamic behaviour is given by combining following attributes:
	 *  1) speed - speed of dynamic delta of sweep angle
	 *  2) rounds - how many rounds to load per animation
	 *  3) animationDuration - how long should animation take
	 */
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
		if (currentFrameAngle < sweepBoundaryAngle) {
			val startShapeStartAngle = startAngle - sweepBoundaryAngle + currentFrameAngle
			ArcUtils.drawArc(canvas, centerPoint, radius, startShapeStartAngle, sweepBoundaryAngle, circlePaint!!)//body
		}

		/**
		 * DYNAMIC SHAPE
		 */
		val inBounds = currentFrameAngle < endAngle
		val sweepAngleDynamic = if (currentFrameAngle < sweepBoundaryAngle) currentFrameAngle else sweepBoundaryAngle
		val startAngleDynamic = if (currentFrameAngle > sweepBoundaryAngle) currentFrameAngle - sweepBoundaryAngle else startAngle
		ArcUtils.drawArc(canvas, centerPoint, radius, startAngleDynamic, sweepAngleDynamic, circlePaint!!, arcsPointsOnCircle, arcsOverlayPoints)

		if (inBounds) {
			invalidate()
		} else {
			drawAction = DrawAction.NONE
		}
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

	fun animateStatic() {
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
		readBackCircleColorFromAttributes(a)
		strokeWidth = a.getDimension(R.styleable.ProgressCircleLayout_strokeWidth, defaultStrokeWidth)
	}

	private fun readBackCircleColorFromAttributes(a: TypedArray) {
		val bc = a.getColorStateList(R.styleable.ProgressCircleLayout_backgroundCircleColor)
		if (bc != null) {
			backCircleColor = bc.defaultColor
		} else {
			backCircleColor = Color.parseColor("#EBF0F6")
		}
	}


	private fun setupPaint() {
		setupBackCirclePaint()
	}

	private fun setupBackCirclePaint() {
		circlePaint = Paint()
		circlePaint!!.color = backCircleColor
		circlePaint!!.style = Paint.Style.STROKE
		circlePaint!!.strokeCap = Paint.Cap.ROUND
		circlePaint!!.strokeWidth = strokeWidth
	}


	fun setInterpolator(i: Interpolator) {
		interpolator = i
	}

}

