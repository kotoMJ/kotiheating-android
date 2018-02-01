package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import com.legalzoom.kollaborate.base.ui.ordersview.CircleAnimItem
import cz.koto.kotiheating.common.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainView {

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

	override fun animateOrderStatus() {
		vmb.binding.orderStatus.animateLayout()
	}
}

interface MainView {
	fun animateOrderStatus()
}
class MainViewModel : ViewModel() {

	var circleItems = ObservableArrayList<CircleAnimItem>()

	init {
		circleItems.add(CircleAnimItem("A", R.color.order_status_color_1, 100))
		circleItems.add(CircleAnimItem("B", R.color.order_status_color_2, 80))
		circleItems.add(CircleAnimItem("C", R.color.order_status_color_3, 60))
		circleItems.add(CircleAnimItem("D", R.color.order_status_color_4, 40))
//		circleItems.add(CircleAnimItem("E", R.color.order_status_color_5, 60))
//		circleItems.add(CircleAnimItem("F", R.color.order_status_color_6, 100))
//		circleItems.add(CircleAnimItem("G", R.color.order_status_color_7, 80))
	}
}

