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

	fun encrypt(applicationContext: Context, valueSet: Set<String>): Set<String> {
		val encryptedSet = mutableSetOf<String>()
		try {
			valueSet.forEach { plainValue ->
				val keys = AesCbcWithIntegrity.generateKeyFromPassword(getSignature(applicationContext), Base64.encode(applicationContext.packageName.toByteArray(), BASE64_FLAGS))
				val cipherTextIvMac = AesCbcWithIntegrity.encrypt(plainValue, keys)
				encryptedSet.add(cipherTextIvMac.toString())
			}
		} catch (th: Throwable) {
			logk("Unable to encrypt $valueSet. $th")
			throw th
		}
		return encryptedSet
	}

	fun decrypt(applicationContext: Context, cipherTextString: String): String {
		return try {
			if (cipherTextString.isBlank()) {
				throw IllegalStateException("Attempt to decrypt empty string! Check issue with source for cipherTextString")
			}
			val keys = AesCbcWithIntegrity.generateKeyFromPassword(getSignature(applicationContext), Base64.encode(applicationContext.packageName.toByteArray(), BASE64_FLAGS))
			val cipherTextIvMac = AesCbcWithIntegrity.CipherTextIvMac(cipherTextString)
			AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys)
		} catch (th: Throwable) {
			logk("Unable to decrypt $cipherTextString. $th")
			return ""
		}
	}

	fun decrypt(applicationContext: Context, cipherTextStringSet: Set<String>): Set<String> {
		val decryptedSet = mutableSetOf<String>()
		try {
			cipherTextStringSet.forEach { cipherTextString ->
				val keys = AesCbcWithIntegrity.generateKeyFromPassword(getSignature(applicationContext), Base64.encode(applicationContext.packageName.toByteArray(), BASE64_FLAGS))
				val cipherTextIvMac = AesCbcWithIntegrity.CipherTextIvMac(cipherTextString)
				decryptedSet.add(AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys))
			}
		} catch (th: Throwable) {
			logk("Unable to decrypt $cipherTextStringSet. $th")
			return emptySet()
		}
		return decryptedSet
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
