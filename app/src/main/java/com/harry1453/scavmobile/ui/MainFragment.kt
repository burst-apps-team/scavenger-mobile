package com.harry1453.scavmobile.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.harry1453.scavmobile.R
import com.harry1453.scavmobile.service.ScavengerService
import java.io.*
import java.util.*

class MainFragment : Fragment() {

    private var viewModel: MainViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val startService = view.findViewById<Button>(R.id.main_startService)
        val stopService = view.findViewById<Button>(R.id.main_stopService)
        val install = view.findViewById<Button>(R.id.main_install)
        val showLog = view.findViewById<Button>(R.id.main_showLog)
        val scavengerOutput = view.findViewById<TextView>(R.id.main_scavengerOutput)

        viewModel!!.getLogFile().observe(this, androidx.lifecycle.Observer { scavengerOutput.text = it })

        startService.setOnClickListener { startScavengerService() }
        stopService.setOnClickListener { stopScavengerService() }
        install.setOnClickListener { installScavenger() }
        showLog.setOnClickListener { showLog() }

        Log.e("MainFragment", "ABI is " + Arrays.toString(Build.SUPPORTED_ABIS))

        return view
    }

    private fun installScavenger() {
        val testDataDir = File(context.getExternalFilesDir(null), "test_data/")
        testDataDir.mkdir()

        copyResourceToInternal(R.raw.config, "config.yaml", false)
        copyResourceToInternal(R.raw.scavenger_x86, "scavenger", true)
        copyResourceToInternal(R.raw.test_plot, "test_data/10282355196851764065_0_8", false)

        Log.e("Log", "Installed!")
        Toast.makeText(context, "Installed!", Toast.LENGTH_LONG).show()
    }

    private fun copyResourceToInternal(resId: Int, internalPath: String, privateStorage: Boolean) {
        val file = File(if (privateStorage) context.filesDir else context.getExternalFilesDir(null), internalPath)
        if (file.exists()) file.delete()
        file.createNewFile()
        val out = file.outputStream()
        val input = resources.openRawResource(resId)
        input.copyTo(out)
        input.close()
        out.close()
    }


    @Throws(IOException::class)
    fun copyDirectory(sourceLocation: File, targetLocation: File) {
        if (sourceLocation.isDirectory) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw IOException("Cannot create dir " + targetLocation.absolutePath)
            }

            val children = sourceLocation.list()
            for (i in children.indices) {
                copyDirectory(
                    File(sourceLocation, children[i]),
                    File(targetLocation, children[i])
                )
            }
        } else {
            targetLocation.setExecutable(true)
            // make sure the directory we plan to store the recording in exists
            val directory = targetLocation.parentFile
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw IOException("Cannot create dir " + directory.absolutePath)
            }

            val `in` = FileInputStream(sourceLocation)
            val out = FileOutputStream(targetLocation)

            // Copy the bits from instream to outstream
            `in`.copyTo(out)
            `in`.close()
            out.close()
        }
    }

    private fun showLog() {
        copyDirectory(context.filesDir, context.getExternalFilesDir(null)!!)

        Log.e("Log", "Copied to " + context.getExternalFilesDir(null)!!.absolutePath)
        Toast.makeText(context, "Copied to " + context.getExternalFilesDir(null)!!.absolutePath, Toast.LENGTH_LONG).show()
    }

    override fun getContext(): Context {
        return super.getContext()!!
    }

    private fun startScavengerService() {
        context.startService(Intent(context, ScavengerService::class.java))
        viewModel!!.watchFile()
    }

    private fun stopScavengerService() {
        context.stopService(Intent(context, ScavengerService::class.java))
    }
}