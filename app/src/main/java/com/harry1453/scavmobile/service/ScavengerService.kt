package com.harry1453.scavmobile.service

import android.app.Service
import android.content.Intent
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.*
import android.app.PendingIntent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.harry1453.scavmobile.R
import com.harry1453.scavmobile.ui.MainActivity
import java.lang.IllegalArgumentException
import kotlin.random.Random


class ScavengerService : Service() {
    var scavengerProcess: Disposable? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("Service", "onStartCommand()")
        if (scavengerProcess == null) {
            Log.e("Service", "Begin...")
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

            val notification = NotificationCompat.Builder(this, getString(R.string.service_channel_id))
                .setContentTitle(getText(R.string.service_notification_title))
                .setContentText(getText(R.string.service_notification_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.service_notification_message))
                .build()

            Log.e("Service", "Show notification...")

            startForeground(Random.nextInt(), notification)

            Log.e("Service", "Starting scavenger...")

            scavengerProcess = startScavenger()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({stopSelf(); Log.e("Service", "Finished OK")}, {stopSelf(); Log.e("Service", "Finished with error", it)})
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("Service", "onCreate()")
    }

    override fun onDestroy() {
        Log.e("Service", "onDestroy()")
        scavengerProcess?.dispose()
        scavengerProcess = null
        super.onDestroy()
    }

    private fun startScavenger(): Completable {
        return Completable.create{
            val extFilesDir = applicationContext.getExternalFilesDir(null)
            val outputPath = File(extFilesDir, "output.txt")

            if (outputPath.exists()) {
                outputPath.delete()
            }
            outputPath.createNewFile()
            val output = BufferedWriter(OutputStreamWriter(outputPath.outputStream()))

            val scavengerConfig = File(extFilesDir, "config.yaml")
            if (!scavengerConfig.exists()) throw IllegalArgumentException("Scavenger configuration not found")

            val scavengerPath = File(filesDir, "scavenger")
            if (!scavengerPath.exists()) throw IllegalArgumentException("Scavenger executable not found")
            scavengerPath.setExecutable(true)
            val scavengerProcess = ProcessBuilder(scavengerPath.absolutePath)
                .directory(extFilesDir)
                .redirectErrorStream(true)
                .start()
            val input = BufferedReader(InputStreamReader(scavengerProcess.inputStream))

            it.setCancellable {
                output.close()
                input.close()
                scavengerProcess.destroy()
                stopSelf()
            }

            input.copyTo(output)
            output.close()
            input.close()
            scavengerProcess.destroy()
            Log.e("Service", "Service finished")
            logToLog()
            stopSelf()
        }
    }

    private fun logToLog() {
        val logFile = File(getExternalFilesDir(null), "output.txt")
        val output = StringWriter()
        val input = InputStreamReader(logFile.inputStream())
        input.copyTo(output)
        input.close()
        Log.e("Service", "Output was $output")
    }
}