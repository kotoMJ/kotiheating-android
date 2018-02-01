package com.legalzoom.kollaborate.base.ui.ordersview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.TextView
import cz.koto.kotiheating.R
import cz.koto.kotiheating.ui.circle.status.CircleStatusView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class CircleStatusLayout : FrameLayout {

	companion object {
		// Workable width is going to be 10 % less than total width
		// 0.45f comes from 90 % / 2, because the radius is half of the workable width
		private const val MAX_RADIUS_MULTIPLIER = 0.45f
		// 11.5 % of the max radius
		private const val CIRCLE_SEPARATION_FACTOR = 0.115f
		private const val CIRCLE_STROKE_WIDTH_FACTOR = 1.75f
		private const val TEXT_LABEL_MARGIN_END_MULTIPLIER = 8
		private const val TEXT_LABEL_ANIMATION_SHIFT_MULTIPLIER = -100f
		const val ANIMATION_SEPARATION_MILLIS = 200L
	}

	private val orderViewsIds: IntArray = intArrayOf(R.id.circle1, R.id.circle2, R.id.circle3, R.id.circle4,
			R.id.circle5, R.id.circle6, R.id.circle7, R.id.circle8)
	private val orderViews: MutableList<CircleStatusView> = mutableListOf()

	private val textViewsIds: IntArray = intArrayOf(R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5, R.id.text6,
			R.id.text7, R.id.text8)
	private val textViews: MutableList<TextView> = mutableListOf()

	// Attributes from layout
	private lateinit var attrs: TypedArray
	lateinit var animItems: List<CircleAnimItem>
	var completeColor: Int? = null
	var uncompleteColor: Int? = null

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
		View.inflate(context, R.layout.circle_status_layout, this)
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
		for (i in 0 until animItems.size) {
			val orderView = findViewById<CircleStatusView>(orderViewsIds[i])
			orderViews.add(orderView)
			textViews.add(findViewById(textViewsIds[i]))
		}
		val circleSeparation = maxRadius * CIRCLE_SEPARATION_FACTOR
		orderViews.forEachIndexed { index, orderStatusView ->
			if (index >= animItems.count()) return
			val radius = maxRadius - (circleSeparation * index)
			orderStatusView.init(context, attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR, animItems[index].colorResId,
					100, animItems[index].progress)

			orderStatusView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
				override fun onGlobalLayout() {

					/**
					 *  Remove this layout listener - as it will run every time the view updates.
					 *  And we don't need to call this repeatedly, since we need this event just to evaluate circles after view is measured.
					 */
					orderStatusView.viewTreeObserver
							.removeOnGlobalLayoutListener(this)

					val textHeight = (textViews[index].bottom - textViews[index].top)
					val textMarginEnd = (orderStatusView.right - orderStatusView.left) / 2 + (TEXT_LABEL_MARGIN_END_MULTIPLIER * resources.displayMetrics.density).toInt()
					val textMarginTop = (orderStatusView.bottom - orderStatusView.top) / 2 - radius - (textHeight / 2)
					val shiftLayoutParams = (textViews[index].layoutParams as ViewGroup.MarginLayoutParams)
					shiftLayoutParams.marginEnd = textMarginEnd
					shiftLayoutParams.topMargin = textMarginTop.toInt()
					textViews[index].layoutParams = shiftLayoutParams
				}
			})
		}
	}

	private fun cleanUpLayout() {
		orderViews.clear()

		(0 until orderViewsIds.size)
				.map { findViewById<CircleStatusView>(orderViewsIds[it]) }
				.forEach { it.cleanView() }
		(0 until textViewsIds.size)
				.map { findViewById<TextView>(textViewsIds[it]) }
				.forEach { it.text = "" }

		textViews.clear()
	}

	fun setInterpolator(i: Interpolator) {
		orderViews.forEach { it.setInterpolator(i) }
	}

	private fun animateCalculatedLayout() {
		textViews.forEach { it.text = "" }

		orderViews.forEachIndexed { index, orderStatusView ->
			if (index >= animItems.count()) return
			orderStatusView.cleanView()
			orderStatusView.showBackgroundInit()
		}

		val delay = ANIMATION_SEPARATION_MILLIS
		Completable.timer(delay, TimeUnit.MILLISECONDS).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
				.subscribe({
					orderViews.forEachIndexed { index, orderStatusView ->
						if (index >= animItems.count()) return@subscribe
						orderStatusView.animateBackground()
					}
				})

		//TODO this timing is workaround in first phase and should be solved (in the second) as chain - when animateBackground finished, start foreground drawing.
		val roughAnimationTimeEstimateInMillis = 1500
		Completable.timer(delay + roughAnimationTimeEstimateInMillis, TimeUnit.MILLISECONDS)
				.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
				.subscribe({
					orderViews.forEachIndexed { index, orderStatusView ->
						if (index >= animItems.count()) return@subscribe
						val delay2 = ANIMATION_SEPARATION_MILLIS * (index + 1L)
						Completable.timer(delay2, TimeUnit.MILLISECONDS)
								.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
								.subscribe({
									orderStatusView.setOnCircleAnimationListener(object : OnCircleAnimationListener {
										override fun onCircleAnimation(currentAnimationValue: Int, maxAnimationValue: Int) {
											if (textViews.size >= index || animItems.size >= index) return
											textViews[index].text = animItems[index].text
											val textColor = if (maxAnimationValue == 100) completeColor else uncompleteColor
											textColor?.let { textViews[index].setTextColor(it) }
										}
									})
									orderStatusView.animateForeground()
									if (textViews.size < index) {
										animateTextLabelTranslation(textViews[index])
									}
								})

					}
				})

	}

	private fun animateTextLabelTranslation(textView: TextView) {
		val shiftFactor = (2 * resources.displayMetrics.density)
		val animator = ValueAnimator.ofFloat(TEXT_LABEL_ANIMATION_SHIFT_MULTIPLIER * shiftFactor, 0f)
		animator.duration = CircleStatusView.DEFAULT_ANIMATION_TIME.toLong()
		animator.interpolator = AccelerateDecelerateInterpolator()
		animator.addUpdateListener { valueAnimator ->
			val shift = valueAnimator.animatedValue as Float
			textView.translationX = shift
		}
		animator.start()
	}
}

