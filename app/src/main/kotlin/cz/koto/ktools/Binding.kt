package cz.koto.ktools

import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import cz.koto.kotiheating.glide.GlideApp

/**
 * Conversion for Visibility - we can pass boolean as parameter in visible property
 */
@BindingConversion
fun convertBooleanToVisibility(visible: Boolean): Int {
	return if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter(value = ["app:imgUrl", "app:imgPlaceholder", "app:imgCircular", "app:roundedCorners"], requireAll = false)
fun loadImage(imageView: ImageView, uri: Uri?, placeholder: Drawable? = null, isCircular: Boolean? = false, roundedCorners: Float? = 0f) {
	val glideRequest = GlideApp
			.with(imageView.context)
			.load(uri)
			.placeholder(placeholder?.let { DrawableCompat.wrap(it) })

	when {
		isCircular != null && isCircular -> {
			glideRequest
					.circleCrop()
					.into(imageView)
		}

		roundedCorners != null && roundedCorners > 0 -> {
			glideRequest
					.transforms(CenterCrop(), RoundedCorners(roundedCorners.toInt()))
					.into(imageView)
		}

		else -> glideRequest.into(imageView)
	}

}

@BindingAdapter("app:srcVector")
fun setSrcVector(view: ImageView, @DrawableRes drawable: Int) {
	view.setImageResource(drawable)
}