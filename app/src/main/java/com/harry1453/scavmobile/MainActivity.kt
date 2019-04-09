package com.harry1453.scavmobile

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    private var prevMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_bottomNavigationView.setOnNavigationItemSelectedListener(this)
        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFragment(Fragment(), getString(R.string.title_home))
        adapter.addFragment(Fragment(), getString(R.string.title_dashboard))
        adapter.addFragment(Fragment(), getString(R.string.title_notifications))
        main_viewPager.adapter = adapter
        main_viewPager.offscreenPageLimit = 3
        main_viewPager.addOnPageChangeListener(this)
        prevMenuItem = main_bottomNavigationView.getMenu().getItem(0)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.navigation_home -> {
                main_viewPager.currentItem = 0
                true
            }
            R.id.navigation_dashboard -> {
                main_viewPager.currentItem = 0
                true
            }
            R.id.navigation_notifications -> {
                main_viewPager.currentItem = 0
                true
            }
            else -> false
        }
    }

    override fun onPageScrollStateChanged(state: Int) = Unit

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

    override fun onPageSelected(position: Int) {
        prevMenuItem?.isChecked = false
        main_bottomNavigationView.menu.getItem(position).isChecked = true
        prevMenuItem = main_bottomNavigationView.menu.getItem(position)
    }

    class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}
