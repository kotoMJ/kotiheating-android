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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import common.log.logk
import cz.koto.kotiheating.R
import cz.koto.kotiheating.databinding.ActivityMainBinding
import cz.koto.kotiheating.ktools.LifecycleAwareBindingRecyclerViewAdapter
import cz.koto.kotiheating.ktools.inject
import cz.koto.kotiheating.ktools.vmb
import cz.koto.kotiheating.rest.HeatingApi
import cz.koto.kotiheating.ui.profile.createProfileDialog
import cz.koto.kotiheating.ui.recycler.SwipeToLeftCallback
import cz.koto.kotiheating.ui.recycler.SwipeToRightCallback
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import retrofit2.HttpException
import java.io.IOException


class MainActivity : AppCompatActivity(), MainView, DialogInterface.OnClickListener {

	private val profileDialog: AlertDialog by lazy {
		createProfileDialog(this, vmb.binding.viewModel!!, vmb.binding.view!!, this)
	}

	private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {
		MainViewModel()
	}
	private var profileMenu: MenuItem? = null

	val heatingApi by inject<HeatingApi>()

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
				updateItem(viewHolder, increase = false)
			}
		}
		val itemTouchLeftHelper = ItemTouchHelper(swipeLeftHandler)
		itemTouchLeftHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)


		val swipeRightHandler = object : SwipeToRightCallback(this, vmb.viewModel) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				updateItem(viewHolder, increase = true)
			}
		}
		val itemTouchRightHelper = ItemTouchHelper(swipeRightHandler)
		itemTouchRightHelper.attachToRecyclerView(vmb.binding.dailyScheduleRecycler)

		checkGoogleAccounts()

	}

	override fun onPostResume() {
		super.onPostResume()
		reloadStatus()
	}

	private fun checkGoogleAccounts() {
		// Configure sign-in to request the user's ID, email address, and basic
		// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(application.getString(R.string.init_client_id))
				.requestProfile()
				.requestEmail()
				.build()

		// Build a GoogleSignInClient with the options specified by gso.
		vmb.viewModel.googleSignInClient = GoogleSignIn.getClient(this, gso);


		// Check for existing Google Sign In account, if the user is already signed in
		// the GoogleSignInAccount will be non-null.
		val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

		if (account != null) {
			vmb.viewModel.googleSignInAccount = account
			updateProfileMenuIcon()
		}
	}

	private fun updateItem(viewHolder: RecyclerView.ViewHolder, increase: Boolean) {
		val position = viewHolder.layoutPosition

		val updatedItem = vmb.binding.viewModel?.statusRequestList?.diffList?.get(position)

		updatedItem?.apply {
			if (increase) {
				temperature += 1
			} else {
				temperature -= 1
			}
		}

		val newList: ArrayList<StatusItem> = ArrayList(vmb.binding.viewModel?.statusRequestList?.diffList?.toList())

		updatedItem?.let {
			newList.set(position, it)
		}

		vmb.binding.viewModel?.statusRequestList?.diffList?.update(newList)

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
		updateProfileMenuIcon()
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		if (item?.itemId == R.id.action_profile) {
			profileDialog.show()
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	override fun onClick(dialog: DialogInterface?, which: Int) {
		profileDialog.dismiss()
	}


	override fun onGoogleSignIn() {
		val signInIntent = vmb.viewModel.googleSignInClient.signInIntent
		startActivityForResult(signInIntent, 33)
	}

	override fun onSignOut() {
		vmb.viewModel.googleSignInClient.signOut()
		vmb.viewModel.googleSignInAccount = null
		updateProfileMenuIcon()
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		super.onActivityResult(requestCode, resultCode, data)

		// Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
		if (requestCode == 33) {
			// The Task returned from this call is always completed, no need to attach
			// a listener.
			val task = GoogleSignIn.getSignedInAccountFromIntent(data)
			handleSignInResult(task)
		}
	}

	private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
		try {
			val account = completedTask.getResult(ApiException::class.java)
			vmb.viewModel.googleSignInAccountError.set("")
			launch(UI) {
				try {
					val res = heatingApi.authorizeGoogleUser(account.idToken)
					vmb.viewModel.googleSignInAccount = account
					logk("authResult=[$res]")
					updateProfileMenuIcon()
				} catch (e: Throwable) {
					logk("e=$e")
					val message = when (e) {
						is IOException -> getString(R.string.auth_io_exception)
						is HttpException -> getString(R.string.auth_http_exception)
						else -> getString(R.string.auth_else)
					}
					vmb.viewModel.googleSignInAccount = null
					vmb.viewModel.googleSignInAccountError.set(message)
					updateProfileMenuIcon()
				}
			}

		} catch (e: ApiException) {
			// The ApiException status code indicates the detailed failure reason.
			// Please refer to the GoogleSignInStatusCodes class reference for more information.
			logk("exception=$e")
			vmb.viewModel.googleSignInAccount = null
			vmb.viewModel.googleSignInAccountError.set(getString(R.string.auth_else))
			updateProfileMenuIcon()
		}

	}

	private fun updateProfileMenuIcon() {
		if (vmb.viewModel.googleSignInAccount == null) {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_outline)
		} else {
			profileMenu?.icon = ContextCompat.getDrawable(this, R.drawable.ic_person_full)
		}
	}
}

interface MainView {
	fun reloadStatus()
	fun onGoogleSignIn()
	fun onSignOut()
	val lifecycleAwareAdapter: LifecycleAwareBindingRecyclerViewAdapter<StatusItem> // TODO: Temp fix for tatarka - remove when tatarka adds support for lifecycle
}

