package cz.kotox.kotiheating.common

import androidx.databinding.BindingAdapter

@BindingAdapter("app:refreshColors")
fun androidx.swiperefreshlayout.widget.SwipeRefreshLayout.setRefreshColors(@Suppress("UNUSED_PARAMETER") setColors: Boolean) {
	setColorSchemeResources(*SWIPE_REFRESH_COLORS)
}
