package cz.koto.kotiheating.common

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import common.log.logk
import cz.koto.kotiheating.common.AesCbcWithIntegrity.BASE64_FLAGS
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

open class SecureWrapper {

	companion object {
		val instance = SecureWrapper()
	}

	fun encrypt(applicationContext: Context, value: String): String {
		return try {
			val keys = AesCbcWithIntegrity.generateKeyFromPassword(getSignature(applicationContext), Base64.encode(applicationContext.packageName.toByteArray(), BASE64_FLAGS))
			val cipherTextIvMac = AesCbcWithIntegrity.encrypt(value, keys)
			cipherTextIvMac.toString()
		} catch (th: Throwable) {
			logk("Unable to encrypt $value. $th")
			throw th
		}
	}

	fun decrypt(applicationContext: Context, cipherTextString: String): String {
		return try {
			val keys = AesCbcWithIntegrity.generateKeyFromPassword(getSignature(applicationContext), Base64.encode(applicationContext.packageName.toByteArray(), BASE64_FLAGS))
			val cipherTextIvMac = AesCbcWithIntegrity.CipherTextIvMac(cipherTextString)
			AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys)
		} catch (th: Throwable) {
			logk("Unable to decrypt $cipherTextString. $th")
			throw th
		}
	}

	open fun getSignature(applicationContext: Context): String {
		try {
			val info = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, PackageManager.GET_SIGNATURES)
			val md = MessageDigest.getInstance("SHA")
			md.update(info.signatures[0].toByteArray())
			val ret = Base64.encodeToString(md.digest(), Base64.DEFAULT)
			return ret
		} catch (ignored: PackageManager.NameNotFoundException) {
			logk("Unable to get signature :$ignored")
		} catch (ignored: NoSuchAlgorithmException) {
			logk("Unable to get signature :$ignored")
		}

		return ""
	}

}
