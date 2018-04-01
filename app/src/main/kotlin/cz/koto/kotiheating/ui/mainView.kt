package cz.koto.kotiheating.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ui.profile.createProfileDialog
import cz.koto.ktools.vmb


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

		setSupportActionBar(vmb.binding.include?.findViewById(R.id.toolbar))
		supportActionBar?.apply {
			setDisplayUseLogoEnabled(false)
			setDisplayShowTitleEnabled(false)
			setDisplayShowHomeEnabled(false)
			setDisplayHomeAsUpEnabled(false)
			setHomeButtonEnabled(false)
		}

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

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {

		when (item?.itemId) {
			R.id.action_profile -> {
				profileDialog.show()
				return true
			}
			R.id.action_clear_all -> {
				vmb.viewModel.revertLocalChanges(day = vmb.viewModel.selectedDay.get())
//				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
//				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
				return true
			}
			R.id.action_anti_freeze -> {
				vmb.viewModel.setLocalTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 5f)
//				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
//				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
				return true
			}
			R.id.action_night_temp -> {
				vmb.viewModel.setLocalTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 15f)
//				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
//				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
				return true
			}
			R.id.action_daily_temp -> {
				vmb.viewModel.setLocalTemperatureTo(day = vmb.viewModel.selectedDay.get(), temp = 23f)
//				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
//				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
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
		vmb.viewModel.signOutGoogleUser { updateProfileMenuIcon() }
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		super.onActivityResult(requestCode, resultCode, data)

		// Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
		if (requestCode == ACTION_SIGN_IN_GOOGLE) {
			// The Task returned from this call is always completed, no need to attach a listener.
			vmb.viewModel.handleSignInGoogleResult(data, { updateProfileMenuIcon() })
		}
	}

//	override fun onDayNext() {
//		vmb.viewModel.selectedDay.set(if (vmb.viewModel.selectedDay.get() + 1 > 6) {
//			0
//		} else vmb.viewModel.selectedDay.get() + 1)
//		(vmb.binding.dailyScheduleRecycler.adapter as BindingRecyclerViewAdapter<StatusItem>).setItems(vmb.viewModel.statusRequestLocalList.diffListMap[vmb.viewModel.selectedDay.get()])
//		vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
//		vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
//	}


	private fun setupViewpager() {
		vmb.binding.mainPager.adapter = MainFragmentAdapter(supportFragmentManager)
		vmb.binding.mainPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(state: Int) {}

			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

			override fun onPageSelected(position: Int) {
				logk(">>>day/position=$position")
				vmb.viewModel.selectedDay.set(position)
			}
		})
	}

	private fun updateProfileMenuIcon() {
		if (vmb.viewModel.isGoogleUserSignedIn()) {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_full)
		} else {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_outline)
		}
	}
}

interface MainActivityView {
	fun onGoogleSignIn()
	fun onSignOut()
}


