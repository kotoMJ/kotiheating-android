package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import com.legalzoom.kollaborate.base.ui.ordersview.CircleStatusAnimItem
import cz.koto.kotiheating.common.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		vmb.binding.orderStatus.setInterpolator(DecelerateInterpolator())
		vmb.binding.orderStatus.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.orderStatus.animateLayout()) {
					vmb.binding.orderStatus.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})
//		vmb.binding.orderStatus.animateLayout()
	}

	fun animateOrderStatus() {
		//vmb.binding.orderStatus.animateLayout()
	}
}

class MainViewModel : ViewModel() {

	var circleItems = ObservableArrayList<CircleStatusAnimItem>()

	init {
		circleItems.add(CircleStatusAnimItem("A", R.color.order_status_color_1, 20))
		circleItems.add(CircleStatusAnimItem("B", R.color.order_status_color_2, 0))
		circleItems.add(CircleStatusAnimItem("C", R.color.order_status_color_3, 40))
		circleItems.add(CircleStatusAnimItem("D", R.color.order_status_color_4, 10))
		circleItems.add(CircleStatusAnimItem("E", R.color.order_status_color_5, 60))
		circleItems.add(CircleStatusAnimItem("F", R.color.order_status_color_6, 100))
		circleItems.add(CircleStatusAnimItem("G", R.color.order_status_color_7, 80))
	}
}

