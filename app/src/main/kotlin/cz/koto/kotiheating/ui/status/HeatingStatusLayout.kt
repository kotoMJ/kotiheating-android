package cz.koto.kotiheating.ui.status

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioButton
import cz.koto.kotiheating.R
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.ui.StatusItem
import cz.koto.ktools.DiffObservableLiveHeatingStatus

class HeatingStatusLayout : FrameLayout {

	companion object {
		// Workable width is going to be 10 % less than total width
		// 0.45f comes from 90 % / 2, because the radius is half of the workable width
		private const val MAX_RADIUS_MULTIPLIER = 0.45f
		// 25 % of the max radius
		private const val CIRCLE_SEPARATION_FACTOR = 0.25f
		private const val CIRCLE_STROKE_WIDTH_FACTOR = 1.2f
	}

	enum class DeviceStatusView {
		SERVER_PROGRESS,
		SERVER_SYNCED,
		REQUEST
	}

	private lateinit var circleViewPm: CircleStatusView
	private lateinit var circleViewAm: CircleStatusView
	private lateinit var centralTextStatusView: TextStatusView

	var statusRequestLocalItemList: DiffObservableLiveHeatingStatus<HeatingDeviceStatus>? = null
		set(value) {
			field = value
			showLayout(true)
		}

	var selectedDay: Int? = null
		set(value) {
			field = value
			showLayout(true)
		}

	lateinit var listToDisplay: List<StatusItem>

	// Attributes from layout
	private lateinit var attrs: TypedArray

	private lateinit var circleNumberUnit: CircleStatusView.CircleNumberUnit

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
		initCustomRadioGroups()
	}

	private fun initCustomRadioGroups() {
		initUnitRadioGroup()
	}

	private fun initUnitRadioGroup() {
		val unitHeatingRadio: RadioButton = findViewById(R.id.heatingUnit)
		val unitTimeRadio: RadioButton = findViewById(R.id.timeUnit)

		unitHeatingRadio.setOnClickListener {
			unitHeatingRadio.isChecked = true
			unitTimeRadio.isChecked = false
			circleNumberUnit = CircleStatusView.CircleNumberUnit.CELSIUS
			showLayout()
		}

		unitTimeRadio.setOnClickListener {
			unitTimeRadio.isChecked = true
			unitHeatingRadio.isChecked = false
			circleNumberUnit = CircleStatusView.CircleNumberUnit.HOURS
			showLayout()
		}

		if (!unitHeatingRadio.isChecked &&
			!unitTimeRadio.isChecked) {
			unitTimeRadio.isChecked = false
			unitHeatingRadio.isChecked = true //Default option
			circleNumberUnit = CircleStatusView.CircleNumberUnit.CELSIUS
		}
	}

	fun showLayout(invokedByValueChange: Boolean = false): Boolean {
		if (measuredWidth != 0) {
			calculateLayout(measuredWidth * MAX_RADIUS_MULTIPLIER, invokedByValueChange)
			showViews()
			return true
		}
		return false
	}

	private fun calculateLayout(maxRadius: Float, invokedByValueChange: Boolean) {
		setProperDataSource(invokedByValueChange)

		val circleSeparation = maxRadius * CIRCLE_SEPARATION_FACTOR

		circleViewPm = findViewById(R.id.circlePm)
		var radius = maxRadius - (circleSeparation * 0)
		circleViewPm.init(attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR, listToDisplay.filter { it.hour > 11 }
			, circleNumberUnit)

		circleViewAm = findViewById(R.id.circleAm)
		radius = maxRadius - (circleSeparation * 1)
		circleViewAm.init(attrs, radius, circleSeparation / CIRCLE_STROKE_WIDTH_FACTOR, listToDisplay.filter { it.hour <= 11 }
			, circleNumberUnit)

		centralTextStatusView = findViewById(R.id.centralTextStatusView)
		statusRequestLocalItemList?.value?.data?.let { heatingDeviceStatus ->
			centralTextStatusView.init(
				attrs = attrs,
				statusItemList = listToDisplay,
				selectedDayShortcut = getShortcutForDay(selectedDay),
				heatingDayShortcut = getShortcutForDay(heatingDeviceStatus.deviceDay),
				name = heatingDeviceStatus.name ?: "",
				mode = getNameForMode(heatingDeviceStatus.deviceMode),
				temperatureWithDegrees = "${heatingDeviceStatus.temperature.div(10)} °C",
				time = "${heatingDeviceStatus.deviceHour}:${heatingDeviceStatus.deviceMinute}")
		}

	}

	private fun setProperDataSource(invokedByValueChange: Boolean) {
		statusRequestLocalItemList?.diffListMap?.get(selectedDay)?.let {
			listToDisplay = it
		}

	}

	private fun showViews() {
		circleViewPm.showView()
		circleViewAm.showView()
		centralTextStatusView.showView()
	}

	private fun getShortcutForDay(day: Int?): String {
		return when (day) {
			0 -> context.getString(R.string.text_status_view_sunday)
			1 -> context.getString(R.string.text_status_view_monday)
			2 -> context.getString(R.string.text_status_view_tuesday)
			3 -> context.getString(R.string.text_status_view_wednesday)
			4 -> context.getString(R.string.text_status_view_thursday)
			5 -> context.getString(R.string.text_status_view_friday)
			6 -> context.getString(R.string.text_status_view_saturday)
			else -> context.getString(R.string.text_status_view_undefined)
		}
	}

	private fun getShortcutForDay(day: String?): String {
		return when (day) {
			"SU" -> context.getString(R.string.text_status_view_saturday)
			"MO" -> context.getString(R.string.text_status_view_monday)
			"TU" -> context.getString(R.string.text_status_view_tuesday)
			"WE" -> context.getString(R.string.text_status_view_wednesday)
			"TH" -> context.getString(R.string.text_status_view_thursday)
			"FR" -> context.getString(R.string.text_status_view_friday)
			"SA" -> context.getString(R.string.text_status_view_saturday)
			else -> context.getString(R.string.text_status_view_undefined)
		}
	}

	private fun getNameForMode(mode: Int?): String {
		return when (mode) {
			2 -> context.getString(R.string.text_status_mode_automatic)
			1 -> context.getString(R.string.text_status_mode_manual)
			else -> context.getString(R.string.text_status_view_undefined)
		}
	}
}

