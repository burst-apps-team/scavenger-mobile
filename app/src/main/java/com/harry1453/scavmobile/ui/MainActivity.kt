package com.harry1453.scavmobile.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.harry1453.scavmobile.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.ArrayList

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    private var prevMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createDefaultNotificationChannel()

        main_bottomNavigationView.setOnNavigationItemSelectedListener(this)
        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFragment(MainFragment(), getString(R.string.title_home))
        adapter.addFragment(ConfigureFragment(), getString(R.string.title_dashboard))
        adapter.addFragment(Fragment(), getString(R.string.title_notifications))
        main_viewPager.adapter = adapter
        main_viewPager.offscreenPageLimit = 2
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
                main_viewPager.currentItem = 1
                true
            }
            R.id.navigation_notifications -> {
                main_viewPager.currentItem = 2
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

    private fun createDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = getString(R.string.service_channel_id)
            val name = getString(R.string.service_channel_name)
            val description = getString(R.string.service_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)!!
            notificationManager.createNotificationChannel(channel)
        }
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
