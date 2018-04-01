package cz.koto.kotiheating.ui

import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.databinding.FragmentMainBinding
import cz.koto.kotiheating.ui.recycler.SwipeToLeftCallback
import cz.koto.kotiheating.ui.recycler.SwipeToRightCallback
import cz.koto.ktools.LifecycleAwareBindingRecyclerViewAdapter
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

		val swipeLeftHandler = object : SwipeToLeftCallback(requireContext(), vmb.viewModel, fragmentDay.get()) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = true, day = fragmentDay.get())
			}
		}
		val itemTouchLeftHelper = ItemTouchHelper(swipeLeftHandler)
		itemTouchLeftHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)


		val swipeRightHandler = object : SwipeToRightCallback(requireContext(), vmb.viewModel, fragmentDay.get()) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = false, day = fragmentDay.get())
			}
		}
		val itemTouchRightHelper = ItemTouchHelper(swipeRightHandler)
		itemTouchRightHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)

	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = vmb.rootView

	private fun updateLocalItem(viewHolder: RecyclerView.ViewHolder, increase: Boolean, day: Int) {
		val position = viewHolder.layoutPosition
		logk(">>>Update item for day=$day")
		vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(day)?.let { dayList ->
			val updatedItem = dayList[position]

			updatedItem?.apply {
				if (increase) {
					temperature += 1
				} else {
					temperature -= 1
				}
			}
			val newList: ArrayList<StatusItem> = ArrayList(vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(day)?.toList())

			updatedItem?.let {
				newList.set(position, it)
			}
			vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(day)?.update(newList)
			vmb.binding.viewModel?.statusRequestLocalList?.value = vmb.binding.viewModel?.statusRequestLocalList?.value
			vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()//TODO probably not needed

			vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
		}

	}


	override fun onResume() {
		super.onResume()
		onReloadStatusView()
	}

	override fun onReloadStatusView() {
		vmb.binding.circleProgress.showLayout()
	}

	override val lifecycleAwareAdapter = LifecycleAwareBindingRecyclerViewAdapter<StatusItem>(this)

	override fun onDayNext() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

interface MainFragmentView {
	fun onDayNext()
	fun onReloadStatusView()
	val lifecycleAwareAdapter: LifecycleAwareBindingRecyclerViewAdapter<StatusItem> // TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle
}