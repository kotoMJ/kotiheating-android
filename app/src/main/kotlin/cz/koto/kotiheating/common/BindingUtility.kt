package cz.koto.kotiheating.common

import android.databinding.BindingAdapter

@BindingAdapter("app:refreshColors")
fun android.support.v4.widget.SwipeRefreshLayout.setRefreshColors(@Suppress("UNUSED_PARAMETER") setColors: Boolean) {
	setColorSchemeResources(*SWIPE_REFRESH_COLORS)
}
