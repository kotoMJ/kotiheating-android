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
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.ArcUtils
import cz.koto.kotiheating.common.getColorForTemperature
import cz.koto.kotiheating.ui.StatusItem
import kotlin.math.round


internal class CircleStatusView : View {

	enum class DrawAction {
		NONE,
		CLEAN_VIEW,
		SHOW_VIEW
	}

	enum class SweepAngleDeltaDirection {
		NONE,
		UP,
		DOWN
	}

	enum class CircleNumberUnit {
		HOURS,
		CELSIUS
	}

	private var interpolator: Interpolator? = null

	private var strokeWidth: Float = 0f
	private var defaultStrokeWidth: Float = 0f
	private var animationDuration: Int = 0


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

	private lateinit var circleNumberUnit: CircleNumberUnit
	private lateinit var statusItemList: List<StatusItem>

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
			 customStrokeWidth: Float,
			 statusItemList: List<StatusItem>,
			 circleNumberUnit: CircleNumberUnit) {
		interpolator = AccelerateDecelerateInterpolator()
		radius = customRadius
		defaultStrokeWidth = customStrokeWidth
		this.statusItemList = statusItemList
		this.circleNumberUnit = circleNumberUnit
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
			DrawAction.SHOW_VIEW -> onDrawAnimateDynamic(canvas, centerPoint)
			else -> logk("CircleStatusView Unimplemented draw action ${drawAction}")
		}


	}

	private fun onDrawCleanView() {
		circlePaint?.alpha = 0
		drawAction = DrawAction.NONE
	}


	private fun onDrawAnimateDynamic(canvas: Canvas, centerPoint: PointF) {
		val animationRounds = 3
		animationDuration = 4000

		val endAngle = 360f * animationRounds
		this.endAngle = endAngle


		if (animationStartTime == 0.toLong()) {
			animationStartTime = System.currentTimeMillis()
		}


		//https://stackoverflow.com/questions/27850634/how-to-draw-an-arc-segment-with-different-fill-and-stroke-colors-for-each-side


		val startCircleNumberAngel = 75f
		var circleNumberPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = Color.parseColor("#313131")
			textSize = 40f//radius * 0.05f//0.66f
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}


		val circleHourSweepAngle = 30f
		var circleHourTextCurrentAngel = 75f
		var circleHourBackgroundAngel = -90f
		circlePaint?.let {

			for (i in 0 until 12) {
				it.color = Color.parseColor(getColorForTemperature(if (statusItemList.size <= i) {
					null
				} else {
					statusItemList[i].temperature
				}))
				val text = if (statusItemList.size <= i) {
					"N/A"
				} else {
					when (circleNumberUnit) {
						CircleNumberUnit.HOURS -> "${statusItemList[i].hour}"
						CircleNumberUnit.CELSIUS -> "${round(statusItemList[i].temperature).toInt()}°"
					}
				}
				ArcUtils.drawArc(canvas, centerPoint, radius, circleHourBackgroundAngel.apply { circleHourBackgroundAngel += circleHourSweepAngle }, circleHourSweepAngle, it)//body
				paintCircleNumber(text, circleHourTextCurrentAngel.apply { circleHourTextCurrentAngel -= circleHourSweepAngle }, canvas, centerPoint, circleNumberPaint)
			}
		}

		drawAction = DrawAction.NONE
	}

	private fun paintCircleNumber(text: String, angleInDegrees: Float, canvas: Canvas, centerPoint: PointF, circleNumberPaint: TextPaint) {
		val angleInRadians = angleInDegrees * (Math.PI / 180)
		var shiftX = -circleNumberPaint.textSize / 2
		var shiftY = +circleNumberPaint.textSize / 2

		canvas.drawText(text,
				(centerPoint.x + radius * Math.cos(angleInRadians) + shiftX).toFloat(),
				(centerPoint.y - radius * Math.sin(angleInRadians) + shiftY).toFloat(),
				circleNumberPaint)
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

	fun showView() {
		resetSweepAngleDelta()
		drawAction = DrawAction.SHOW_VIEW
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

