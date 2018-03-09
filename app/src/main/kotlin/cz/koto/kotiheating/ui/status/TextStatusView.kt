package cz.koto.kotiheating.ui.status

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.support.v4.content.res.ResourcesCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.getCurrentHour
import cz.koto.kotiheating.ktools.DiffObservableListLiveData
import cz.koto.kotiheating.ui.StatusItem


internal class TextStatusView : View {

	enum class DrawAction {
		NONE,
		CLEAN_VIEW,
		SHOW_VIEW
	}

	private var initialized: Boolean = false
	private var drawAction: DrawAction = DrawAction.NONE
	private lateinit var statusList: DiffObservableListLiveData<StatusItem>

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

	fun init(attrs: TypedArray, statusItemMap: DiffObservableListLiveData<StatusItem>) {
		this.statusList = statusItemMap
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
			else -> logk(">>> Unimplemented draw action ${drawAction}")
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


		var centerTextPaint = TextPaint().apply {
			isDither = true
			isAntiAlias = true
			color = Color.parseColor("#313131")
			textSize = 140f//radius * 0.05f//0.66f
			typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
		}


		val text = "${statusList.value?.data?.get(getCurrentHour())?.temperature?.toInt() ?: "N/A"}°C"
		canvas.drawText(text,
				centerPoint.x - getHorizontalCenterDelta(text, centerTextPaint.typeface, centerTextPaint.textSize),
				centerPoint.y + getVerticalCenterDelta(centerTextPaint.textSize),
				centerTextPaint)

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

