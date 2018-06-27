package cz.koto.kotiheating.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.common.compareLists
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ui.profile.createProfileDialog
import cz.koto.kotiheating.ui.recycler.SwipeToLeftCallback
import cz.koto.kotiheating.ui.recycler.SwipeToRightCallback
import cz.koto.ktools.LifecycleAwareBindingRecyclerViewAdapter
import cz.koto.ktools.vmb
import kotlinx.android.synthetic.main.activity_main.view.*
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter


class MainActivity : AppCompatActivity(), MainActivityView, DialogInterface.OnClickListener {

	companion object {
		private const val ACTION_SIGN_IN_GOOGLE = 1
	}

	private val profileDialog: AlertDialog by lazy {
		createProfileDialog(this, vmb.binding.viewModel!!, vmb.binding.view!!, this)
	}

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}
	private var profileMenu: MenuItem? = null
	private var revertChangesMenu: MenuItem? = null
	private var setToFreezeMenu: MenuItem? = null
	private var setToNightMenu: MenuItem? = null
	private var setToDayMenu: MenuItem? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setSupportActionBar(vmb.binding.toolbar)
		supportActionBar?.apply {
			setDisplayUseLogoEnabled(false)
			setDisplayShowTitleEnabled(false)
			setDisplayShowHomeEnabled(false)
			setDisplayHomeAsUpEnabled(false)
			setHomeButtonEnabled(false)
		}

		setupRecycler()
		setupViewpager()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		profileMenu = menu.findItem(R.id.action_profile)
		revertChangesMenu = menu.findItem(R.id.action_clear_all)
		setToFreezeMenu = menu.findItem(R.id.action_anti_freeze)
		setToNightMenu = menu.findItem(R.id.action_night_temp)
		setToDayMenu = menu.findItem(R.id.action_daily_temp)
		updateProfileMenuIcon()
		return super.onCreateOptionsMenu(menu)
	}

	override fun onPostResume() {
		super.onPostResume()
		updateFab()
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {

		when (item?.itemId) {
			R.id.action_profile -> {
				profileDialog.show()
				return true
			}
			R.id.action_clear_all -> {
				vmb.viewModel.revertLocalChanges(day = vmb.viewModel.selectedDay)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				updateFab()
				return true
			}
			R.id.action_anti_freeze -> {
				vmb.viewModel.setLocalTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 50)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				updateFab()
				return true
			}
			R.id.action_night_temp -> {
				vmb.viewModel.setLocalTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 150)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				updateFab()
				return true
			}
			R.id.action_daily_temp -> {
				vmb.viewModel.setLocalTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 230)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				updateFab()
				return true
			}
		}

		return super.onOptionsItemSelected(item);
	}

	override fun onClick(dialog: DialogInterface?, which: Int) {
		profileDialog.dismiss()
	}


	override fun onGoogleSignIn() {
		startActivityForResult(vmb.viewModel.getSignInGoogleIntent(), ACTION_SIGN_IN_GOOGLE)
	}

	override fun onSignOut() {
		vmb.viewModel.signOutGoogleUser {
			updateProfileMenuIcon()
		}
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		super.onActivityResult(requestCode, resultCode, data)

		// Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
		if (requestCode == ACTION_SIGN_IN_GOOGLE) {
			// The Task returned from this call is always completed, no need to attach a listener.
			vmb.viewModel.handleSignInGoogleResult(data, {
				updateProfileMenuIcon()
			})
		}
	}

	override val lifecycleAwareAdapter = LifecycleAwareBindingRecyclerViewAdapter<StatusItem>(this)

	private fun setupRecycler() {
		val swipeLeftHandler = object : SwipeToLeftCallback(this, vmb.viewModel, vmb.viewModel.selectedDay) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = true, day = vmb.viewModel.selectedDay.get())
			}
		}
		val itemTouchLeftHelper = ItemTouchHelper(swipeLeftHandler)
		itemTouchLeftHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)


		val swipeRightHandler = object : SwipeToRightCallback(this, vmb.viewModel, vmb.viewModel.selectedDay) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = false, day = vmb.viewModel.selectedDay.get())
			}
		}
		val itemTouchRightHelper = ItemTouchHelper(swipeRightHandler)
		itemTouchRightHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)

	}

	private fun setupViewpager() {
		vmb.binding.mainPager.offscreenPageLimit = 1
		vmb.binding.mainPager.adapter = MainFragmentAdapter(supportFragmentManager)
		vmb.binding.mainPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(state: Int) {}

			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

			override fun onPageSelected(position: Int) {
				vmb.viewModel.selectedDay.set(position)
				//TODO is there a better (automatic) way how to force reload of adapter base on day change?
				val adapter = vmb.binding.dailyScheduleRecycler.adapter as BindingRecyclerViewAdapter<StatusItem>
				adapter.setItems(vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(position))
			}
		})
	}

	private fun updateLocalItem(viewHolder: RecyclerView.ViewHolder, increase: Boolean, day: Int) {
		val position = viewHolder.layoutPosition
		vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(day)?.let { dayList ->
			val updatedItem = dayList[position]
			updatedItem?.apply {
				if (increase) {
					temperature += 10
				} else {
					temperature -= 10
				}
			}
			val newList: ArrayList<StatusItem> = ArrayList(vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(day)?.toList())

			updatedItem?.let {
				newList.set(position, it)
			}

			vmb.binding.viewModel?.statusRequestLocalList?.value?.data?.timetable?.get(day)?.set(position, updatedItem.temperature)
			vmb.binding.viewModel?.statusRequestLocalList?.diffListMap?.get(day)?.update(newList)
			vmb.binding.viewModel?.statusRequestLocalList?.value = vmb.binding.viewModel?.statusRequestLocalList?.value
			vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()//This is necessary to refresh colored recycler item.
		}
		updateFab()

	}

	private fun updateFab() {
		if (compareLists(vmb.viewModel.statusRequestLocalList.diffListMap.get(vmb.viewModel.selectedDay.get())
						?: emptyList(),
						vmb.viewModel.statusRequestRemoteList.diffListMap.get(vmb.viewModel.selectedDay.get())
								?: emptyList()) == 0) {
			vmb.binding.fabSend.visibility = View.GONE
		} else {

			vmb.binding.fabSend.show()//showWithAnimation()
		}
	}


	private fun updateProfileMenuIcon() {
		if (vmb.viewModel.isGoogleUserSignedIn()) {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_full)
		} else {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_outline)
		}
	}

	override fun onSend() {
		vmb.binding.fabSend.isEnabled = false
		vmb.binding.fabSend.setImageDrawable(ContextCompat.getDrawable(baseContext, R.drawable.ic_sync))
		try {
			vmb.viewModel.sendRequestForSchedule()?.let {
				vmb.viewModel.updateLocalList(it)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()//This is necessary to refresh colored recycler item.
			}
		} catch (ise: IllegalStateException) {
			Snackbar.make(vmb.rootView.coordinate, "User has no heating deice assigned", Snackbar.LENGTH_LONG).show()
		} catch (th: Throwable) {
			logk("Unable to send request! $th")
		}
		vmb.binding.fabSend.setImageDrawable(ContextCompat.getDrawable(baseContext, R.drawable.ic_send))
		vmb.binding.fabSend.isEnabled = true
		updateFab()


	}
}

interface MainActivityView {
	fun onGoogleSignIn()
	fun onSignOut()
	fun onSend()
	val lifecycleAwareAdapter: LifecycleAwareBindingRecyclerViewAdapter<StatusItem> // TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle
}


