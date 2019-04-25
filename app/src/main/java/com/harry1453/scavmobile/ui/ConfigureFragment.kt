package com.harry1453.scavmobile.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import burst.kit.burst.BurstCrypto
import com.harry1453.scavmobile.R
import com.harry1453.scavmobile.entity.ScavengerConfiguration
import kotlinx.android.synthetic.main.fragment_configure.*
import org.yaml.snakeyaml.error.YAMLException
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.math.BigInteger
import java.util.*

class ConfigureFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_configure, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configure_save.setOnClickListener { saveConfig() }
        loadConfig()
    }

    override fun getContext(): Context { // TODO hacky :(
        return super.getContext()!!
    }

    fun getConfigFile(): File {
        return File(context.getExternalFilesDir(null), "config.yaml")
    }

    fun loadConfig() {
        var config: ScavengerConfiguration
        try {
            val configFileReader = getConfigFile().bufferedReader()
            config = ScavengerConfiguration.fromYaml(configFileReader)
            configFileReader.close()
        } catch (e: IOException) {
            config = ScavengerConfiguration()
        } catch (e: YAMLException) {
            e.printStackTrace()
            Toast.makeText(context, "Could not parse config YAML", Toast.LENGTH_LONG).show()
            return
        }

        var passphrase = ""
        config.account_id_to_secret_phrase.values.forEach { newPassphrase ->
            passphrase = newPassphrase
        }

        configure_passphrase.setText(passphrase)
        configure_url.setText(config.url)
        configure_target_deadline.setText(config.target_deadline.toString())
    }

    fun saveConfig() {
        try {
            val uint64Max = BigInteger("18446744073709551615")
            val burstCrypto = BurstCrypto.getInstance()

            val passphrase = configure_passphrase.text.toString()
            val plotFiles = StringTokenizer(configure_plotfiles.text.toString(), "\n")
            val url = configure_url.text.toString()
            val targetDeadline = BigInteger(configure_target_deadline.text.toString())



            if (uint64Max > targetDeadline) {
                Toast.makeText(context, "Target deadline must be less than 2^16", Toast.LENGTH_LONG).show()
                return
            }

            val config = ScavengerConfiguration()
            config.account_id_to_secret_phrase[BigInteger(burstCrypto.getBurstAddressFromPassphrase(passphrase).id)] = passphrase
            config.url = url
            config.target_deadline = targetDeadline
            while (plotFiles.hasMoreTokens()) {
                val plotFile = plotFiles.nextToken()
                if (plotFile == "") continue
                config.plot_dirs.add(plotFile)
            }

            val configFileWriter = getConfigFile().bufferedWriter()
            configFileWriter.write(config.toYaml())
            configFileWriter.close()

            // TODO toast saved
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save configuration", Toast.LENGTH_LONG).show()
        }
    }
}