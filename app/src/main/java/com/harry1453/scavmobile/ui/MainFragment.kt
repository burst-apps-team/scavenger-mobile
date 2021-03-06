package com.harry1453.scavmobile.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.harry1453.scavmobile.R
import com.harry1453.scavmobile.service.ScavengerService
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.*
import java.util.*

class MainFragment : Fragment() {

    private var viewModel: MainViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel!!.getLogFile().observe(this, androidx.lifecycle.Observer { main_scavengerOutput.text = it })

        main_startService.setOnClickListener { startScavengerService() }
        main_stopService.setOnClickListener { stopScavengerService() }
        main_install.setOnClickListener { installScavenger() }
        main_showLog.setOnClickListener { showLog() }

        Log.e("MainFragment", "ABI is " + Arrays.toString(Build.SUPPORTED_ABIS))
    }

    private fun installScavenger() {
        val testDataDir = File(context.getExternalFilesDir(null), "test_data/")
        testDataDir.mkdir()

        val abis = Build.SUPPORTED_ABIS
        if (abis.isEmpty()) {
            Toast.makeText(context, "Could not determine your device's architecture.", Toast.LENGTH_LONG).show()
        }
        val abi = abis[0].toLowerCase(Locale.ENGLISH)
        val scavengerResource = when(abi) {
            "x86" -> R.raw.scavenger_x86
            "arm64-v8a" -> R.raw.scavenger_aarch64
            else -> {
                Toast.makeText(context, "Architecture $abi not supported.", Toast.LENGTH_LONG).show()
                return
            }
        }

        copyResourceToInternal(R.raw.config, "config.yaml", false)
        copyResourceToInternal(scavengerResource, "scavenger", true)
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
        viewModel!!.stopWatchingFile()
    }
}