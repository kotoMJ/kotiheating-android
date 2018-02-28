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

	var statusItemMap: List<StatusItem> = listOf(

			StatusItem(15f, 0),
			StatusItem(15f, 1),
			StatusItem(15f, 2),
			StatusItem(15f, 3),
			StatusItem(15f, 4),
			StatusItem(15f, 5),
			StatusItem(15f, 6),
			StatusItem(15f, 7),
			StatusItem(15f, 8),
			StatusItem(15f, 9),
			StatusItem(15f, 10),
			StatusItem(16f, 11),

			StatusItem(17f, 12),
			StatusItem(18f, 13),
			StatusItem(19f, 14),
			StatusItem(20f, 15),
			StatusItem(21f, 16),
			StatusItem(22f, 17),
			StatusItem(23f, 18),
			StatusItem(23f, 19),
			StatusItem(23f, 20),
			StatusItem(22f, 21),
			StatusItem(15f, 22),
			StatusItem(15f, 23))

}

