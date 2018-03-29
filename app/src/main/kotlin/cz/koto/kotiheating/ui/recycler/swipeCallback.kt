package cz.koto.kotiheating.ui.recycler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.getColorForTemperature
import cz.koto.kotiheating.ui.MainViewModel

abstract class SwipeToLeftCallback(context: Context, private val mainViewModel: MainViewModel) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

	private val maximumTempValue = 30f
	private val decreaseIcon = ContextCompat.getDrawable(context, R.drawable.ic_increase)
	private val intrinsicWidth = decreaseIcon?.intrinsicWidth ?: 0
	private val intrinsicHeight = decreaseIcon?.intrinsicHeight ?: 0
	private val background = ColorDrawable()
	private val backgroundColor = Color.parseColor(getColorForTemperature(maximumTempValue))

	override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
		mainViewModel.statusRequestLocalList.diffListMap[mainViewModel.selectedDay.get()]?.let { diffObservableList ->
			viewHolder?.adapterPosition?.let {
				if (diffObservableList[it].temperature > maximumTempValue - 1) {
					return 0
				}
			}
		}

		return super.getMovementFlags(recyclerView, viewHolder)
	}

	override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
		return false
	}

	override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
		val itemView = viewHolder.itemView
		val itemHeight = itemView.bottom - itemView.top

		// Draw the background
		background.color = backgroundColor
		background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
		background.draw(c)

		// Calculate position of delete icon
		val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
		val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
		val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
		val deleteIconRight = itemView.right - deleteIconMargin
		val deleteIconBottom = deleteIconTop + intrinsicHeight

		// Draw the decrease icon
		decreaseIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
		decreaseIcon?.draw(c)

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
	}
}

abstract class SwipeToRightCallback(context: Context, private val mainViewModel: MainViewModel) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

	private val minimumTempValue = 5f
	private val increaseIcon = ContextCompat.getDrawable(context, R.drawable.ic_decrease)
	private val intrinsicWidth = increaseIcon?.intrinsicWidth ?: 0
	private val intrinsicHeight = increaseIcon?.intrinsicHeight ?: 0
	private val background = ColorDrawable()
	private val backgroundColor = Color.parseColor(getColorForTemperature(minimumTempValue))

	override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
		mainViewModel.statusRequestLocalList.diffListMap[mainViewModel.selectedDay.get()]?.let { diffObservableList ->
			viewHolder?.adapterPosition?.let {
				if (diffObservableList[it].temperature < minimumTempValue + 1) {
					return 0
				}
			}
		}

		return super.getMovementFlags(recyclerView, viewHolder)
	}

	override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
		return false
	}

	override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
		val itemView = viewHolder.itemView
		val itemHeight = itemView.bottom - itemView.top

		// Draw the background
		background.color = backgroundColor
		background.setBounds(itemView.left - dX.toInt(), itemView.top, itemView.right, itemView.bottom)
		background.draw(c)

		// Calculate position of delete icon
		val increaseIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
		val increaseIconMargin = (itemHeight - intrinsicHeight)
		val increaseIconLeft = itemView.left + increaseIconMargin - intrinsicWidth
		val increaseIconRight = itemView.left + increaseIconMargin
		val increaseIconBottom = increaseIconTop + intrinsicHeight

		// Draw the increase icon
		increaseIcon?.setBounds(increaseIconLeft, increaseIconTop, increaseIconRight, increaseIconBottom)
		increaseIcon?.draw(c)

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
	}
}
