package cz.kotox.kotiheating.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import cz.kotox.kotiheating.R
import cz.kotox.kotiheating.databinding.ActivityMainBinding
import cz.kotox.kotiheating.ui.profile.createProfileDialog
import cz.kotox.kotiheating.ui.recycler.SwipeToLeftCallback
import cz.kotox.kotiheating.ui.recycler.SwipeToRightCallback
import cz.kotox.ktools.LifecycleAwareBindingRecyclerViewAdapter
import cz.kotox.ktools.vmb
import kotlinx.android.synthetic.main.activity_main.view.coordinate
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import java.util.Calendar

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

	override fun onResume() {
		setTabToCurrentDay()
		refresh()
		super.onResume()
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
				vmb.viewModel.revertLocalChanges()
				vmb.binding.dailyScheduleRecycler.adapter?.notifyDataSetChanged()
				updateFab()
				return true
			}
			R.id.action_anti_freeze -> {
				vmb.viewModel.setLocalDailyTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 50)
				vmb.binding.dailyScheduleRecycler.adapter?.notifyDataSetChanged()
				updateFab()
				return true
			}
			R.id.action_night_temp -> {
				vmb.viewModel.setLocalDailyTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 150)
				vmb.binding.dailyScheduleRecycler.adapter?.notifyDataSetChanged()
				updateFab()
				return true
			}
			R.id.action_daily_temp -> {
				vmb.viewModel.setLocalDailyTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 230)
				vmb.binding.dailyScheduleRecycler.adapter?.notifyDataSetChanged()
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

	private fun refresh() {
		vmb.binding.viewModel?.refreshDataFromServer()
		vmb.binding.dailyScheduleRecycler.adapter?.notifyDataSetChanged()//This is necessary to refresh colored recycler item.
	}

	override fun onSignOut() {
		vmb.viewModel.signOutGoogleUser {
			updateProfileMenuIcon()
			refresh()
		}
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		// Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
		if (requestCode == ACTION_SIGN_IN_GOOGLE) {
			// The Task returned from this call is always completed, no need to attach a listener.
			vmb.viewModel.handleSignInGoogleResult(data) {
				updateProfileMenuIcon()
				refresh()
			}
		}
	}

	override val lifecycleAwareAdapter = LifecycleAwareBindingRecyclerViewAdapter<StatusItem>(this)

	private fun setTabToCurrentDay() {
		vmb.binding.mainPager.currentItem = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
	}

	private fun setupRecycler() {
		val swipeLeftHandler = object : SwipeToLeftCallback(this, vmb.viewModel, vmb.viewModel.selectedDay) {
			override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = true, day = vmb.viewModel.selectedDay.get())
			}
		}
		val itemTouchLeftHelper = ItemTouchHelper(swipeLeftHandler)
		itemTouchLeftHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)

		val swipeRightHandler = object : SwipeToRightCallback(this, vmb.viewModel, vmb.viewModel.selectedDay) {
			override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = false, day = vmb.viewModel.selectedDay.get())
			}
		}
		val itemTouchRightHelper = ItemTouchHelper(swipeRightHandler)
		itemTouchRightHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)

	}

	private fun setupViewpager() {
		vmb.binding.mainPager.offscreenPageLimit = 1
		vmb.binding.mainPager.adapter = MainFragmentAdapter(supportFragmentManager)
		vmb.binding.mainPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(state: Int) {}

			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

			override fun onPageSelected(position: Int) {
				vmb.viewModel.selectedDay.set(position)
				//TODO is there a better (automatic) way how to force reload of adapter base on day change?
				val adapter = vmb.binding.dailyScheduleRecycler.adapter as BindingRecyclerViewAdapter<StatusItem>
				adapter.setItems(vmb.binding.viewModel?.statusRequestList?.diffListMap?.get(position))
			}
		})
	}

	private fun updateLocalItem(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, increase: Boolean, day: Int) {
		val position = viewHolder.layoutPosition
		if (increase) vmb.viewModel.increaseLocalHourlyTemperatureTo(day, position) else vmb.viewModel.decreaseLocalHourlyTemperatureTo(day, position)
		vmb.binding.dailyScheduleRecycler.adapter?.notifyDataSetChanged()//This is necessary to refresh colored recycler item.
		updateFab()

	}

	private fun updateFab() {

		if (vmb.binding.viewModel?.differLocalRequestFromRemote() == true) {
			vmb.binding.fabSend.show()
		} else {
			vmb.binding.fabSend.hide()
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

		vmb.viewModel.sendRequestForSchedule().fold({

			when (it) {
				is IllegalStateException -> {
					//TODO localize
					Snackbar.make(vmb.rootView.coordinate, "User has no heating deice assigned", Snackbar.LENGTH_LONG).show()
				}
				else -> {
					//TODO localize
					Snackbar.make(vmb.rootView.coordinate, "Unexpected issue when setting new schedule", Snackbar.LENGTH_LONG).show()
				}
			}

		}, {
			vmb.viewModel.updateRequestListWithServerResponse(it)
		})
		vmb.binding.fabSend.setImageDrawable(ContextCompat.getDrawable(baseContext, R.drawable.ic_send))
		updateFab()
		vmb.binding.fabSend.isEnabled = true

	}
}

interface MainActivityView {
	fun onGoogleSignIn()
	fun onSignOut()
	fun onSend()
	val lifecycleAwareAdapter: LifecycleAwareBindingRecyclerViewAdapter<StatusItem> // TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle
}