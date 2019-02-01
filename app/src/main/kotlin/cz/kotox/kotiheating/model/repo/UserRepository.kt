package cz.kotox.kotiheating.model.repo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import common.log.logk
import cz.kotox.kotiheating.BR
import cz.kotox.kotiheating.R
import cz.kotox.kotiheating.common.SecureWrapper
import cz.kotox.kotiheating.model.entity.HEATING_SET
import cz.kotox.kotiheating.model.entity.USER_KEY
import cz.kotox.kotiheating.model.rest.HeatingUserApi
import cz.kotox.ktools.inject
import cz.kotox.ktools.sharedPrefs
import cz.kotox.ktools.string
import cz.kotox.ktools.stringSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserRepository : BaseRepository() {

	private val application by inject<Application>()
	val heatingApi by inject<HeatingUserApi>()

	private var heatingSetPref by application.sharedPrefs().stringSet(key = HEATING_SET)
	private var userKeyPref by application.sharedPrefs().string(key = USER_KEY)

	var heatingSet: Set<String> = setOf()
		get() {
			return if (field.isEmpty()) {
				//field = SecureWrapper.instance.decrypt(application, heatingSetPref ?: emptySet())
				field = heatingSetPref ?: emptySet()
				field
			} else field
		}
		set(value) {
			field = value
			heatingSetPref = value
		}

	var userKey: String = ""
		get() {
			return if (field.isBlank()) {
				field = SecureWrapper.instance.decrypt(application, userKeyPref ?: "")
				field
			} else {
				field
			}
		}
		set(value) {
			field = value
			userKeyPref = value
		}

	var googleSignInAccount: GoogleSignInAccount? = null
		@Bindable get
		set(value) {
			field = value
			notifyPropertyChanged(BR.googleSignInAccount)
		}

	val googleEmail: String
		@Bindable("googleSignInAccount")
		get() = googleSignInAccount?.email ?: "demo@profile.com"

	val googleName: String
		@Bindable("googleSignInAccount")
		get() = googleSignInAccount?.displayName ?: "Demo User"

	lateinit var googleSignInClient: GoogleSignInClient

	var googleSignInAccountError: ObservableField<String> = ObservableField()

	@SuppressLint("RestrictedApi")
	fun handleSignInResult(signInGoogleResultIntent: Intent?, credentialsHasChanged: () -> Unit/*updateProfileMenuIcon()*/) {
		try {
			val completedTask = GoogleSignIn.getSignedInAccountFromIntent(signInGoogleResultIntent)
			val account = completedTask.getResult(ApiException::class.java)
			googleSignInAccountError.set("")
			GlobalScope.launch(Dispatchers.Main) {
				try {
					val heatingAuth = heatingApi.authorizeGoogleUser(account!!.idToken)
					heatingAuth?.let {
						it.heatingSet?.let { hs ->
							heatingSet = hs
						}
						it.userKey?.let { uk ->
							userKeyPref = SecureWrapper.instance.encrypt(application, uk)
						}

						googleSignInAccount = account
						credentialsHasChanged.invoke()
						return@launch
					}
					cleanUpGoogleUser({ credentialsHasChanged.invoke() })
					googleSignInAccountError.set(application.getString(R.string.auth_unable_encrypt))

				} catch (e: Throwable) {
					logk("e=$e")
					val message = when (e) {
						is IOException -> application.getString(R.string.auth_io_exception)
						is HttpException -> application.getString(R.string.auth_http_exception)
						else -> application.getString(R.string.auth_else)
					}
					cleanUpGoogleUser({ credentialsHasChanged.invoke() })
					googleSignInAccountError.set(message)
				}
			}

		} catch (e: ApiException) {
			// The ApiException status code indicates the detailed failure reason.
			// Please refer to the GoogleSignInStatusCodes class reference for more information.
			val errorMessage = when (e.statusCode) {
				12500 -> {
					logk("Update Google Play services on device! Or fix SHA1 in developers console! exception=$e")
					application.getString(R.string.auth_google_services_sign_in_failed)
				}
				7 -> {
					application.getString(R.string.auth_google_services_network_error)
				}
				else -> {
					logk("Unexpected exception=$e")
					application.getString(R.string.auth_else)
				}
			}
			cleanUpGoogleUser({ credentialsHasChanged.invoke() })
			googleSignInAccountError.set(errorMessage)
		}

	}

	private fun cleanUpGoogleUser(credsentialsHasChanged: () -> Unit) {
		googleSignInAccount = null
		heatingSetPref = emptySet()
		heatingSet = emptySet()
		userKeyPref = ""
		userKey = ""
		credsentialsHasChanged.invoke()
	}

	@SuppressLint("RestrictedApi")
	//https://developers.google.com/android/guides/releases#march_20_2018_-_version_1200
	fun signOutGoogleUser(credentialsHasChanged: () -> Unit) {
		googleSignInClient.signOut()
		cleanUpGoogleUser(credentialsHasChanged)
	}

	@SuppressLint("RestrictedApi")
	//https://developers.google.com/android/guides/releases#march_20_2018_-_version_1200
	fun checkGoogleAccounts() {
		// Configure sign-in to request the user's ID, email address, and basic
		// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(application.getString(R.string.init_client_id))
			.requestProfile()
			.requestEmail()
			.build()

		// Build a GoogleSignInClient with the options specified by gso.
		googleSignInClient = GoogleSignIn.getClient(application, gso);

		// Check for existing Google Sign In account, if the user is already signed in
		// the GoogleSignInAccount will be non-null.
		val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(application)

		if (account != null) {
			googleSignInAccount = account
		}
	}

}