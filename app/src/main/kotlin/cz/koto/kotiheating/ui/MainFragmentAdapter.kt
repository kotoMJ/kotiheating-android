package cz.koto.kotiheating.ui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class MainFragmentAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

	private val TAB_COUNT = 7

	override fun getCount(): Int {
		return TAB_COUNT
	}

	override fun getItem(position: Int): Fragment {
		if (position in 0..6) {
			return MainFragment.newInstance(position)
		} else {
			throw IllegalStateException("Unsupported position in MainFragmentAdapter! Only 0..6 are allowed.")
		}
	}
}