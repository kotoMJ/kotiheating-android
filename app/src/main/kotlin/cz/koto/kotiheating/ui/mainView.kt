package cz.koto.kotiheating.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import cz.koto.kotiheating.R
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ui.profile.createProfileDialog
import cz.koto.kotiheating.ui.recycler.SwipeToLeftCallback
import cz.koto.kotiheating.ui.recycler.SwipeToRightCallback
import cz.koto.ktools.LifecycleAwareBindingRecyclerViewAdapter
import cz.koto.ktools.vmb


class MainActivity : AppCompatActivity(), MainView, DialogInterface.OnClickListener {

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

		vmb.binding.circleProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.circleProgress.showLayout()) {
					vmb.binding.circleProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})

		val swipeLeftHandler = object : SwipeToLeftCallback(this, vmb.viewModel) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = true)
			}
		}
		val itemTouchLeftHelper = ItemTouchHelper(swipeLeftHandler)
		itemTouchLeftHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)


		val swipeRightHandler = object : SwipeToRightCallback(this, vmb.viewModel) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateLocalItem(viewHolder, increase = false)
			}
		}
		val itemTouchRightHelper = ItemTouchHelper(swipeRightHandler)
		itemTouchRightHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)

	}

	override fun onPostResume() {
		super.onPostResume()
		reloadStatus()
	}


	private fun updateLocalItem(viewHolder: RecyclerView.ViewHolder, increase: Boolean) {
		val position = viewHolder.layoutPosition

		val updatedItem = vmb.binding.viewModel?.statusRequestLocalList?.diffList?.get(position)

		updatedItem?.apply {
			if (increase) {
				temperature += 1
			} else {
				temperature -= 1
			}
		}

		val newList: ArrayList<StatusItem> = ArrayList(vmb.binding.viewModel?.statusRequestLocalList?.diffList?.toList())

		updatedItem?.let {
			newList.set(position, it)
		}

		vmb.binding.viewModel?.statusRequestLocalList?.diffList?.update(newList)

		vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
		vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
	}

	override fun reloadStatus() {
		vmb.binding.circleProgress.showLayout()
	}

	override val lifecycleAwareAdapter = LifecycleAwareBindingRecyclerViewAdapter<StatusItem>(this)

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
				vmb.viewModel.revertLocalChanges()
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
				return true
			}
			R.id.action_anti_freeze -> {
				vmb.viewModel.setLocalTemperatureTo(5f)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
				return true
			}
			R.id.action_night_temp -> {
				vmb.viewModel.setLocalTemperatureTo(15f)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
				return true
			}
			R.id.action_daily_temp -> {
				vmb.viewModel.setLocalTemperatureTo(23f)
				vmb.binding.dailyScheduleRecycler.adapter.notifyDataSetChanged()
				vmb.binding.circleProgress.showLayout(invokedByValueChange = true)
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


	private fun updateProfileMenuIcon() {
		if (vmb.viewModel.isGoogleUserSignedIn()) {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_full)
		} else {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_outline)
		}
	}
}

interface MainView {
	fun reloadStatus()
	fun onGoogleSignIn()
	fun onSignOut()
	val lifecycleAwareAdapter: LifecycleAwareBindingRecyclerViewAdapter<StatusItem> // TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle
}


