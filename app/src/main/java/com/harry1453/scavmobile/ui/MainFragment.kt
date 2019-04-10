package com.harry1453.scavmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.harry1453.scavmobile.R
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        main_startService.setOnClickListener { startService() }
        main_stopService.setOnClickListener { stopService() }

        return view
    }

    private fun startService() {

    }

    private fun stopService() {

    }
}