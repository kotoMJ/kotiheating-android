package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import cz.koto.kotiheating.common.getColorForTemperature
import cz.koto.kotiheating.common.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainView {

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		vmb.binding.circleProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.circleProgress.showLayout()) {
					vmb.binding.circleProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})
	}

	override fun reloadStatus() {
		vmb.binding.circleProgress.showLayout()
	}
}

interface MainView {
	fun reloadStatus()
}

class MainViewModel : ViewModel() {

	var statusItemMap: HashMap<Int, Float> = hashMapOf(

			0 to 15f,
			1 to 15f,
			2 to 15f,
			3 to 15f,
			4 to 15f,
			5 to 15f,
			6 to 15f,
			7 to 15f,
			8 to 15f,
			9 to 15f,
			10 to 15f,
			11 to 16f,

			12 to 17f,
			13 to 18f,
			14 to 19f,
			15 to 20f,
			16 to 21f,
			17 to 22f,
			18 to 23f,
			19 to 23f,
			20 to 23f,
			21 to 22f,
			22 to 15f,
			23 to 15f)

}

