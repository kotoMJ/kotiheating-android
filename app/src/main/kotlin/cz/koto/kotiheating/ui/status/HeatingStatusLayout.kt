package cz.koto.kotiheating.ui.status

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioButton
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.compareLists
import cz.koto.kotiheating.model.entity.HeatingSchedule
import cz.koto.kotiheating.ui.StatusItem
import cz.koto.ktools.DiffObservableLiveHeatingSchedule


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

	var statusDeviceItemList: DiffObservableLiveHeatingSchedule<HeatingSchedule>? = null
		set(value) {
			field = value
			showLayout(true)
		}

	var statusRequestLocalItemList: DiffObservableLiveHeatingSchedule<HeatingSchedule>? = null
		set(value) {
			field = value
			showLayout(true)
		}

	var currentDay: Int? = null
		set(value) {
			field = value
			showLayout(true)
		}

	lateinit var listToDisplay: List<StatusItem>


	// Attributes from layout
	private lateinit var attrs: TypedArray

	private lateinit var circleNumberUnit: CircleStatusView.CircleNumberUnit

	private var deviceStatusView: DeviceStatusView = DeviceStatusView.SERVER_PROGRESS

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
		initStatusRadioGroup()
		initUnitRadioGroup()
	}

	private fun initUnitRadioGroup() {
		val unitHeatingRadio: RadioButton = findViewById(R.id.heatingUnit)
		val unitTimeRadio: RadioButton = findViewById(R.id.timeUnit)

		unitHeatingRadio.setOnClickListener({
			unitHeatingRadio.isChecked = true
			unitTimeRadio.isChecked = false
			circleNumberUnit = CircleStatusView.CircleNumberUnit.CELSIUS
			showLayout()
		})

		unitTimeRadio.setOnClickListener({
			unitTimeRadio.isChecked = true
			unitHeatingRadio.isChecked = false
			circleNumberUnit = CircleStatusView.CircleNumberUnit.HOURS
			showLayout()
		})

		if (!unitHeatingRadio.isChecked &&
				!unitTimeRadio.isChecked) {
			unitTimeRadio.isChecked = false
			unitHeatingRadio.isChecked = true //Default option
			circleNumberUnit = CircleStatusView.CircleNumberUnit.CELSIUS
		}
	}

	private fun initStatusRadioGroup() {

		val statusDeviceProgressRadio: RadioButton = findViewById(R.id.deviceStatusProgress)
		val statusDeviceSyncedRadio: RadioButton = findViewById(R.id.deviceStatusSynced)
		val statusRequestRadio: RadioButton = findViewById(R.id.requestStatus)

		statusDeviceProgressRadio.setOnClickListener({
			deviceStatusView = DeviceStatusView.SERVER_PROGRESS
			updateStatusRadio(statusDeviceProgressRadio, statusDeviceSyncedRadio, statusRequestRadio)
		})

		statusDeviceSyncedRadio.setOnClickListener({
			deviceStatusView = DeviceStatusView.SERVER_SYNCED
			updateStatusRadio(statusDeviceProgressRadio, statusDeviceSyncedRadio, statusRequestRadio)
		})

		statusRequestRadio.setOnClickListener({
			deviceStatusView = DeviceStatusView.REQUEST
			updateStatusRadio(statusDeviceProgressRadio, statusDeviceSyncedRadio, statusRequestRadio)
		})


	}

	private fun updateStatusRadio(statusDeviceProgressRadio: RadioButton, statusDeviceSyncedRadio: RadioButton, statusRequestRadio: RadioButton) {
		when (deviceStatusView) {
			DeviceStatusView.SERVER_PROGRESS -> {
				statusDeviceProgressRadio.isChecked = true
				statusDeviceSyncedRadio.isChecked = false
				statusRequestRadio.isChecked = false
			}
			DeviceStatusView.SERVER_SYNCED -> {
				statusDeviceSyncedRadio.isChecked = true
				statusDeviceProgressRadio.isChecked = false
				statusRequestRadio.isChecked = false
			}
			DeviceStatusView.REQUEST -> {
				statusRequestRadio.isChecked = true
				statusDeviceProgressRadio.isChecked = false
				statusDeviceSyncedRadio.isChecked = false
			}
		}
		showLayout()
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
		centralTextStatusView.init(attrs, listToDisplay, currentDay)

	}

	private fun setProperDataSource(invokedByValueChange: Boolean) {

		val (statusDeviceProgressRadio: RadioButton, statusDeviceSyncedRadio: RadioButton, statusRequestRadio: RadioButton)
				= updateStatusRadioVisibility(invokedByValueChange)

		if (statusDeviceProgressRadio.isChecked || statusDeviceSyncedRadio.isChecked) {
			statusDeviceItemList?.diffListMap?.get(currentDay)?.let {
				listToDisplay = it
			}
		} else if (statusRequestRadio.isChecked) {
			statusRequestLocalItemList?.diffListMap?.get(currentDay)?.let {
				listToDisplay = it
			}
		} else {
			throw IllegalStateException("Unexpected state of status radio group!")
		}
	}

	private fun updateStatusRadioVisibility(invokedByValueChange: Boolean): Triple<RadioButton, RadioButton, RadioButton> {
		val statusDeviceProgressRadio: RadioButton = findViewById(R.id.deviceStatusProgress)
		val statusDeviceSyncedRadio: RadioButton = findViewById(R.id.deviceStatusSynced)
		val statusRequestRadio: RadioButton = findViewById(R.id.requestStatus)

		if (compareLists(statusRequestLocalItemList?.diffListMap?.get(currentDay) ?: emptyList(),
						statusDeviceItemList?.diffListMap?.get(currentDay) ?: emptyList()) == 0) {
			statusDeviceProgressRadio.visibility = View.GONE
			statusRequestRadio.visibility = View.GONE
			statusDeviceSyncedRadio.visibility = View.VISIBLE
			statusDeviceSyncedRadio.isChecked = true
		} else {
			statusDeviceProgressRadio.visibility = View.VISIBLE
			statusRequestRadio.visibility = View.VISIBLE
			statusDeviceSyncedRadio.visibility = View.GONE

			if (invokedByValueChange) {
				statusRequestRadio.isChecked = true
				statusDeviceProgressRadio.isChecked = false
				statusDeviceSyncedRadio.isChecked = false
			} else if (!statusDeviceProgressRadio.isChecked &&
					!statusDeviceSyncedRadio.isChecked &&
					!statusRequestRadio.isChecked) {
				statusRequestRadio.isChecked = true //Default option
			}
		}
		return Triple(statusDeviceProgressRadio, statusDeviceSyncedRadio, statusRequestRadio)
	}


	private fun showViews() {
		circleViewPm.showView()
		circleViewAm.showView()
		centralTextStatusView.showView()
	}

}

