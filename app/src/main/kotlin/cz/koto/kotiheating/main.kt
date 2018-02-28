package cz.koto.kotiheating

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import cz.koto.kotiheating.ktools.vmb
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ui.status.StatusItem
import me.tatarka.bindingcollectionadapter2.ItemBinding

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

//	val itemBinding = ItemBinding.of<WalletOverview>(BR.item, R.layout.item_dashboard)
//			.bindExtra(BR.viewModel, this)
//			.bindExtra(BR.listener, itemClickCallback)!!

	var statusItemMap: HashMap<Int, StatusItem> = hashMapOf(

			0 to StatusItem(15f),
			1 to StatusItem(15f),
			2 to StatusItem(15f),
			3 to StatusItem(15f),
			4 to StatusItem(15f),
			5 to StatusItem(15f),
			6 to StatusItem(15f),
			7 to StatusItem(15f),
			8 to StatusItem(15f),
			9 to StatusItem(15f),
			10 to StatusItem(15f),
			11 to StatusItem(16f),

			12 to StatusItem(17f),
			13 to StatusItem(18f),
			14 to StatusItem(19f),
			15 to StatusItem(20f),
			16 to StatusItem(21f),
			17 to StatusItem(22f),
			18 to StatusItem(23f),
			19 to StatusItem(23f),
			20 to StatusItem(23f),
			21 to StatusItem(22f),
			22 to StatusItem(15f),
			23 to StatusItem(15f))

}

