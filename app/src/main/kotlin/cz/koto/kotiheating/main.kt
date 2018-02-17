package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import cz.koto.kotiheating.common.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainView {

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		vmb.binding.circleProgress.setInterpolator(DecelerateInterpolator()/*LinearInterpolator()*/)
		vmb.binding.circleProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.circleProgress.animateLayout()) {
					vmb.binding.circleProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})
	}

	override fun reloadStatus() {
		vmb.binding.circleProgress.animateLayout()
	}
}

interface MainView {
	fun reloadStatus()
}

class MainViewModel : ViewModel() {


}

