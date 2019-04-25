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
    private var scavengerProcess: Disposable? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (scavengerProcess == null) {
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

            val notification = NotificationCompat.Builder(this, getString(R.string.service_channel_id))
                .setContentTitle(getText(R.string.service_notification_title))
                .setContentText(getText(R.string.service_notification_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.service_notification_message))
                .build()

            startForeground(Random.nextInt(), notification)

            scavengerProcess = startScavenger()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({stopSelf(); Log.e("Service", "Finished OK")}, {stopSelf(); Log.e("Service", "Finished with error", it)})
        }

        return START_STICKY
    }

    override fun onDestroy() {
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
                scavengerProcess.destroy()
            }

            try {
                val buffer = CharArray(8 * 1024)
                var chars = input.read(buffer)
                val output = StringWriter()
                while (chars >= 0) {
                    val string = String(buffer, 0, chars)
                    output.write(string)
                    output.flush()
                    val outputFile = BufferedWriter(FileWriter(outputPath)) // TODO this is a hack...
                    outputFile.write(output.toString())
                    outputFile.flush()
                    outputFile.close()
                    chars = input.read(buffer)
                }
            } catch (ignored: InterruptedIOException) {}

            input.close()
            scavengerProcess.destroy()
            stopSelf()
        }
    }
}