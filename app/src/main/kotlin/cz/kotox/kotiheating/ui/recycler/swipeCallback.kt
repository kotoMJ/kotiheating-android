package cz.kotox.kotiheating.ui.recycler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.ItemTouchHelper
import common.log.logk
import cz.kotox.kotiheating.R
import cz.kotox.kotiheating.common.getColorForTemperature
import cz.kotox.kotiheating.ui.MainViewModel

abstract class SwipeToLeftCallback(context: Context, private val mainViewModel: MainViewModel, private val day: ObservableInt) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

	private val maximumTempValue = 300
	private val decreaseIcon = ContextCompat.getDrawable(context, R.drawable.ic_increase)
	private val intrinsicWidth = decreaseIcon?.intrinsicWidth ?: 0
	private val intrinsicHeight = decreaseIcon?.intrinsicHeight ?: 0
	private val background = ColorDrawable()
	private val backgroundColor = Color.parseColor(getColorForTemperature(maximumTempValue))

	override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
		mainViewModel.statusRequestList.diffListMap[day.get()]?.let { diffObservableList ->
			viewHolder.adapterPosition.let {
				if (diffObservableList.size > it) {
					if (diffObservableList[it].temperature > maximumTempValue - 10) {
						return 0
					}
				} else {
					logk("Array Left inconsistency! unexpected index $it")
					return 0
				}
			}
		}

		return super.getMovementFlags(recyclerView, viewHolder)
	}

	override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
		return false
	}

	override fun onChildDraw(c: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
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

abstract class SwipeToRightCallback(context: Context, private val mainViewModel: MainViewModel, private val day: ObservableInt) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

	private val minimumTempValue = 50
	private val increaseIcon = ContextCompat.getDrawable(context, R.drawable.ic_decrease)
	private val intrinsicWidth = increaseIcon?.intrinsicWidth ?: 0
	private val intrinsicHeight = increaseIcon?.intrinsicHeight ?: 0
	private val background = ColorDrawable()
	private val backgroundColor = Color.parseColor(getColorForTemperature(minimumTempValue))

	override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
		mainViewModel.statusRequestList.diffListMap[day.get()]?.let { diffObservableList ->
			viewHolder.adapterPosition.let {
				if (diffObservableList.size > it) {
					if (diffObservableList[it].temperature < minimumTempValue + 10) {
						return 0
					}
				} else {
					logk("Array Right inconsistency! unexpected index $it")
					return 0
				}
			}
		}

		return super.getMovementFlags(recyclerView, viewHolder)
	}

	override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
		return false
	}

	override fun onChildDraw(c: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
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
