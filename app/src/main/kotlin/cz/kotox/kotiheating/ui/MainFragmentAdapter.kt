package cz.kotox.kotiheating.ui

class MainFragmentAdapter(fragmentManager: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager) {

	companion object {
		const val TAB_COUNT = 7
	}

	override fun getCount(): Int {
		return TAB_COUNT
	}

	override fun getItem(position: Int): androidx.fragment.app.Fragment {
		if (position in 0 until TAB_COUNT) {
			return MainFragment.newInstance(position)
		} else {
			throw IllegalStateException("Unsupported position in MainFragmentAdapter! Only 0..$TAB_COUNT are allowed.")
		}
	}
}