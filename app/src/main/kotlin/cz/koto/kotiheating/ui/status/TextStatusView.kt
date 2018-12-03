package cz.koto.kotiheating.ui.status

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.adjustAlpha
import cz.koto.kotiheating.ui.StatusItem

internal class TextStatusView : View {

	enum class DrawAction {
		NONE,
		CLEAN_VIEW,
		SHOW_VIEW
	}

	private var initialized: Boolean = false
	private var drawAction: DrawAction = DrawAction.NONE
	private lateinit var statusList: List<StatusItem>
	private var selectedDayShortcut: String = ""
	private var name: String = ""
	private var mode: String = ""
	private var dateTime: String = ""
	private var temperatureWithDegrees: String = ""
	private var highlightDetails: Boolean = false
	private val textColorAlpha = adjustAlpha(Color.parseColor("#313131"), 0.5f)
	private val textColorHighlight = Color.parseColor("#313131")

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

	fun init(attrs: TypedArray, statusItemList: List<StatusItem>, selectedDayShortcut: String, heatingDayShortcut: String,
		name: String, mode: String, time: String, temperatureWithDegrees: String) {
		this.statusList = statusItemList
		this.selectedDayShortcut = selectedDayShortcut
		this.name = name
		this.mode = mode
		this.dateTime = if (heatingDayShortcut == selectedDayShortcut) time else "$heatingDayShortcut $time"
		this.temperatureWithDegrees = temperatureWithDegrees
		this.highlightDetails = heatingDayShortcut == selectedDayShortcut
		readAttributesAndSetupFields(attrs)
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
			DrawAction.SHOW_VIEW -> onDrawTextStatus(canvas, centerPoint)
			else -> logk(">>> TextStatusView Unimplemented draw action ${drawAction}")
		}

	}

	private fun onDrawCleanView() {
		drawAction = DrawAction.NONE
	}

	private fun onDrawTextStatus(canvas: Canvas, centerPoint: PointF) {

		fun getHorizontalCenterDelta(text: String, typeface: Typeface, fontSize: Float): Float {
			val p = Paint()
			p.typeface = typeface
			p.textSize = fontSize
			val textWidth = p.measureText(text)
			return textWidth / 2f
		}

		fun getVerticalCenterDelta(fontSize: Float): Float {
			return fontSize / 4 //This is magic, empiric value
		}

		val dayOfWeekTextPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = textColorHighlight
			textSize = 140f//radius * 0.05f//0.66f
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}

		//val text = "${statusList.get(getCurrentHour())?.temperature?.toInt() ?: "N/A"}°C"
		val text = selectedDayShortcut

		canvas.drawText(text,
			centerPoint.x - getHorizontalCenterDelta(text, dayOfWeekTextPaint.typeface, dayOfWeekTextPaint.textSize),
			centerPoint.y + getVerticalCenterDelta(dayOfWeekTextPaint.textSize),
			dayOfWeekTextPaint)

		/*******************************************/

		val upperTextPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = textColorHighlight
			textSize = dayOfWeekTextPaint.textSize / 3
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}

		val upperText = name

		canvas.drawText(upperText,
			centerPoint.x - getHorizontalCenterDelta(upperText, upperTextPaint.typeface, upperTextPaint.textSize),
			centerPoint.y + getVerticalCenterDelta(upperTextPaint.textSize - dayOfWeekTextPaint.textSize * 4.5f),
			upperTextPaint)

		/*******************************************/

		val upper2TextPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = textColorAlpha
			textSize = dayOfWeekTextPaint.textSize / 3
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}

		val upper2Text = mode

		canvas.drawText(upper2Text,
			centerPoint.x - getHorizontalCenterDelta(upper2Text, upper2TextPaint.typeface, upper2TextPaint.textSize),
			centerPoint.y + getVerticalCenterDelta(upper2TextPaint.textSize - dayOfWeekTextPaint.textSize * 3),
			upper2TextPaint)

		/*******************************************/

		val belowTextPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = textColorAlpha
			textSize = dayOfWeekTextPaint.textSize / 3
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}

		val belowText = dateTime

		canvas.drawText(belowText,
			centerPoint.x - getHorizontalCenterDelta(belowText, belowTextPaint.typeface, belowTextPaint.textSize),
			centerPoint.y + getVerticalCenterDelta(belowTextPaint.textSize + dayOfWeekTextPaint.textSize * 2.5f),
			belowTextPaint)

		/*******************************************/

		val below2TextPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = if (highlightDetails) textColorHighlight else textColorAlpha
			textSize = dayOfWeekTextPaint.textSize / 2
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}

		val below2Text = temperatureWithDegrees

		canvas.drawText(below2Text,
			centerPoint.x - getHorizontalCenterDelta(below2Text, below2TextPaint.typeface, below2TextPaint.textSize),
			centerPoint.y + getVerticalCenterDelta(below2TextPaint.textSize + dayOfWeekTextPaint.textSize * 4.5f),
			below2TextPaint)
	}

	fun showView() {
		drawAction = DrawAction.SHOW_VIEW
		invalidate()
	}

	fun cleanView() {
		drawAction = DrawAction.CLEAN_VIEW
		invalidate()
	}

	private fun readAttributesAndSetupFields(attrs: TypedArray) {
		//strokeWidth = a.getDimension(R.styleable.ProgressCircleLayout_strokeWidth, defaultStrokeWidth)
	}

}

