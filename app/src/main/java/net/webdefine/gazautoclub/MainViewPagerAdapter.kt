package net.webdefine.gazautoclub

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

class MainViewPagerAdapter(fragmentManager : FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    private val fragments = listOf(
                MainFragment.newInstance(0),
                MainFragment.newInstance(1),
                MainFragment.newInstance(2)
            )
    private lateinit var currentFragment: MainFragment

    override fun getCount() = fragments.size

    override fun getItem(position: Int) = fragments[position]

    override fun setPrimaryItem(container: ViewGroup?, position: Int, `object`: Any) {
        if (currentFragment !== `object`) {
            currentFragment = `object` as MainFragment
        }
        super.setPrimaryItem(container, position, `object`)
    }
}