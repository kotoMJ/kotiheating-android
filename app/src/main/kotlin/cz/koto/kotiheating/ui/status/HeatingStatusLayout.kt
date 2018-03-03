package cz.koto.kotiheating.ui.status

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioButton
import cz.koto.kotiheating.R
import cz.koto.kotiheating.ktools.DiffObservableListLiveData


class HeatingStatusLayout : FrameLayout {

	companion object {
		// Workable width is going to be 10 % less than total width
		// 0.45f comes from 90 % / 2, because the radius is half of the workable width
		private const val MAX_RADIUS_MULTIPLIER = 0.45f
		// 25 % of the max radius
		private const val CIRCLE_SEPARATION_FACTOR = 0.25f
		private const val CIRCLE_STROKE_WIDTH_FACTOR = 1.2f
	}

	private lateinit var circleViewPm: CircleStatusView
	private lateinit var circleViewAm: CircleStatusView
	private lateinit var centralTextStatusView: TextStatusView
	lateinit var statusItemMap: DiffObservableListLiveData<StatusItem>

	// Attributes from layout
	private lateinit var attrs: TypedArray

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
		setCustomRadioGroups()
	}

	private fun setCustomRadioGroups() {
		val statusDeviceProgressRadio: RadioButton = findViewById(R.id.deviceStatusProgress)
		val statusDeviceSyncedRadio: RadioButton = findViewById(R.id.deviceStatusSynced)
		val statusRequestRadio: RadioButton = findViewById(R.id.requestStatus)

		statusDeviceProgressRadio.setOnClickListener({
			statusDeviceProgressRadio.isChecked = true
			statusDeviceSyncedRadio.isChecked = false
			statusRequestRadio.isChecked = false
		})

		statusDeviceSyncedRadio.setOnClickListener({
			statusDeviceSyncedRadio.isChecked = true
			statusDeviceProgressRadio.isChecked = false
			statusRequestRadio.isChecked = false
		})

		statusRequestRadio.setOnClickListener({
			statusRequestRadio.isChecked = true
			statusDeviceProgressRadio.isChecked = false
			statusDeviceSyncedRadio.isChecked = false
		})

		val unitHeatingRadio: RadioButton = findViewById(R.id.heatingUnit)
		val unitTimeRadio: RadioButton = findViewById(R.id.timeUnit)

		unitHeatingRadio.setOnClickListener({
			unitHeatingRadio.isChecked = true
			unitTimeRadio.isChecked = false
		})

		unitTimeRadio.setOnClickListener({
			unitTimeRadio.isChecked = true
			unitHeatingRadio.isChecked = false
		})

	}

	fun showLayout(): Boolean {
		if (measuredWidth != 0) {
			calculateLayout(measuredWidth * MAX_RADIUS_MULTIPLIER)
			showViews()
			return true
		}
		return false
	}

	private fun calculateLayout(maxRadius: Float) {
		val circleSeparation = maxRadius * CIRCLE_SEPARATION_FACTOR

		circleViewPm = findViewById(R.id.circlePm)
		var radius = maxRadius - (circleSeparation * 0)
		circleViewPm.init(attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR, statusItemMap.value?.data?.filter { it.hour > 11 }
				?: emptyList())

		circleViewAm = findViewById(R.id.circleAm)
		radius = maxRadius - (circleSeparation * 1)
		circleViewAm.init(attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR, statusItemMap.value?.data?.filter { it.hour <= 11 }
				?: emptyList())

		centralTextStatusView = findViewById(R.id.centralTextStatusView)
		centralTextStatusView.init(attrs, statusItemMap)
	}


	private fun showViews() {
		circleViewPm.showView()
		circleViewAm.showView()
		centralTextStatusView.showView()
	}

}

