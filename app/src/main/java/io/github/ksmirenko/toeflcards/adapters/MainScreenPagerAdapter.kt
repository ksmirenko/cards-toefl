package io.github.ksmirenko.toeflcards.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.github.ksmirenko.toeflcards.R
import io.github.ksmirenko.toeflcards.layout.DictionaryFragment
import io.github.ksmirenko.toeflcards.layout.ModuleListFragment

/**
 * Adapter for pages of the main screen (list of modules and dictionary).
 */
class MainScreenPagerAdapter(fm: FragmentManager, private val context: Context)
: FragmentPagerAdapter(fm) {

    override fun getCount() = 2

    override fun getItem(position: Int) =
        when (position) {
            0 -> ModuleListFragment()
            1 -> DictionaryFragment()
            else -> Fragment()
        }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.practice)
            1 -> context.getString(R.string.dictionary)
            else -> ""
        }
    }
}