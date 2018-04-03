package cz.koto.kotiheating.ui

import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import cz.koto.kotiheating.R
import cz.koto.kotiheating.databinding.FragmentMainBinding
import cz.koto.ktools.vmb


class MainFragment : Fragment(), MainFragmentView {

	companion object {
		fun newInstance(day: Int) = MainFragment().apply { arguments = Bundle().apply { putInt("day", day) } }
	}

	private val vmb by vmb<MainViewModel, FragmentMainBinding>(R.layout.fragment_main) { MainViewModel() }

	val fragmentDay = ObservableInt(-1)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.getInt("day")?.let {
			fragmentDay.set(it)
		}
		if (fragmentDay.get() < 0) throw IllegalStateException("MainFragment cannot be initialized without day!")

		vmb.binding.circleProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.circleProgress.showLayout()) {
					vmb.binding.circleProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = vmb.rootView


	override fun onResume() {
		super.onResume()
		onReloadStatusView()
	}

	override fun onReloadStatusView() {
		vmb.binding.circleProgress.showLayout()
	}


	override fun onDayNext() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

interface MainFragmentView {
	fun onDayNext()
	fun onReloadStatusView()
}