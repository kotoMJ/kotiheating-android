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
import com.legalzoom.kollaborate.base.ui.ordersview.OnCircleAnimationListener
import common.log.log
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.ArcUtils


internal class CircleLoaderView : View {

	enum class DrawAction {
		NONE,
		CLEAN_VIEW,
		ANIMATE_STATIC
	}

	companion object {
		val DEFAULT_ANIMATION_TIME = 1000
	}

	private var interpolator: Interpolator? = null
	private var listener: OnCircleAnimationListener? = null


	private var startValue: Int = 0
	private var endValue: Int = 0

	private var strokeWidth: Float = 0f
	private var defaultStrokeWidth: Float = 0f
	private var animationDuration: Int = 0
	private var backCircleColor: Int = 0

	private var animateOnDisplay: Boolean = false

	private var animationSpeed: Float = 0f

	// Head and tail paints are used to properly draw arc ends, that is, the beginning shouldn't be rounded
	// but the end should. So we create a head of #headAngle degrees in order to achieve this behavior.
	private var backCirclePaintHead: Paint? = null
	private var backCirclePaintTail: Paint? = null
	private var currentAngle: Float = 0.toFloat()

	private var endAngle: Float = 0f
	private var animationStartTime: Long = 0

	private val maxAngle: Float = 270f
	private val headAngle: Float = 5f

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
//				if (drawAction == DrawAction.FOREGROUND_ANIMATE) {
//					listener?.onCircleAnimation(getCurrentAnimationFrameValue(interpolatedPathGone), currentValue)
//				}
			} else {
				currentAngle = endAngle
//				if (drawAction == DrawAction.FOREGROUND_ANIMATE) {
//					listener?.onCircleAnimation(getCurrentAnimationFrameValue(1.0f), currentValue)
//				}
			}

			return currentAngle
		}

	constructor(context: Context) : super(context) {
		interpolator = AccelerateDecelerateInterpolator()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

	fun init(context: Context,
			 attrs: TypedArray,
			 customRadius: Float,
			 customStrokeWidth: Float,
			 customEndValue: Int) {
		interpolator = AccelerateDecelerateInterpolator()
		radius = customRadius
		defaultStrokeWidth = customStrokeWidth
		endValue = customEndValue
		readAttributesAndSetupFields(attrs)

		setupPaint()
		initialized = true
	}

	private fun drawInitBackground(canvas: Canvas, centerPoint: PointF) {
		val startBackgroundAngel = -90f
		val endBackgroundAngel = 90f
		ArcUtils.drawArc(canvas, centerPoint, radius, startBackgroundAngel, headAngle, backCirclePaintHead!!)
		ArcUtils.drawArc(canvas, centerPoint, radius, startBackgroundAngel + headAngle, endBackgroundAngel - headAngle, backCirclePaintTail!!)
	}

	private fun drawCompleteBackground(canvas: Canvas, centerPoint: PointF) {
		val startBackgroundAngel = -90f
		val endBackgroundAngel = 270f
		ArcUtils.drawArc(canvas, centerPoint, radius, startBackgroundAngel, headAngle, backCirclePaintHead!!)
		ArcUtils.drawArc(canvas, centerPoint, radius, startBackgroundAngel + headAngle, endBackgroundAngel - headAngle, backCirclePaintTail!!)
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
			DrawAction.ANIMATE_STATIC -> onDrawAnimateBackground(canvas, centerPoint)
			else -> log(">>> Unimplemented draw action ${drawAction}")
		}


	}

	private fun onDrawCleanView() {
		backCirclePaintHead?.alpha = 0
		backCirclePaintTail?.alpha = 0
		drawAction = DrawAction.NONE
	}


	private fun onDrawAnimateBackground(canvas: Canvas, centerPoint: PointF) {
		val startBackgroundAngel = 0f
		val endBackgroundAngel = 180f
		endAngle = endBackgroundAngel

		if (animationStartTime == 0.toLong()) {
			animationStartTime = System.currentTimeMillis()
		}

		drawInitBackground(canvas, centerPoint)

		val inBounds = animateOnDisplay && currentFrameAngle < endBackgroundAngel

		val sweepAngleBack = if (inBounds) currentFrameAngle else endBackgroundAngel
		ArcUtils.drawArc(canvas, centerPoint, radius, startBackgroundAngel, maxOf(sweepAngleBack, headAngle), backCirclePaintHead!!, 360, true)
		ArcUtils.drawArc(canvas, centerPoint, radius, startBackgroundAngel + headAngle, maxOf(sweepAngleBack - headAngle, 0f), backCirclePaintTail!!, 360, true)

		if (inBounds) {
			invalidate()
		} else {
			drawAction = DrawAction.NONE
		}
	}

	fun animateStatic() {
		drawAction = DrawAction.ANIMATE_STATIC
		animateOnDisplay = true
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

		computeEndAngle()

		setAnimationSpeed()

	}

	private fun applyAttributes(a: TypedArray) {
		startValue = a.getInt(R.styleable.ProgressCircleLayout_startValue, 0)

		animateOnDisplay = a.getBoolean(R.styleable.ProgressCircleLayout_animateOnDisplay, true)

		animationDuration = a.getInt(R.styleable.ProgressCircleLayout_animationDuration, DEFAULT_ANIMATION_TIME)

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

	private fun setAnimationSpeed() {
		val seconds = animationDuration.toFloat() / 1000
		val i = (seconds * 60).toInt()
		animationSpeed = endAngle / i
	}

	private fun computeEndAngle(): Float {
//		val totalLength = endValue - startValue
//		val pathGone = currentValue - startValue
//		val v = pathGone.toFloat() / totalLength
//		if (paintBackgroundOnly()) {
			endAngle = 270f
//		} else {
//			endAngle = (maxAngle * v)
//		}
		return endAngle
	}


	private fun setupPaint() {
		setupBackCirclePaint()
	}

	private fun setupBackCirclePaint() {
		backCirclePaintHead = Paint()
		backCirclePaintHead!!.color = backCircleColor
		backCirclePaintHead!!.style = Paint.Style.STROKE
		backCirclePaintHead!!.strokeWidth = strokeWidth

		backCirclePaintTail = Paint()
		backCirclePaintTail!!.color = backCircleColor
		backCirclePaintTail!!.style = Paint.Style.STROKE
		backCirclePaintTail!!.strokeCap = Paint.Cap.ROUND
		backCirclePaintTail!!.strokeWidth = strokeWidth
	}

	fun setOnCircleAnimationListener(l: OnCircleAnimationListener) {
		listener = l
	}


	fun setInterpolator(i: Interpolator) {
		interpolator = i
	}

}

