package com.harry1453.scavmobile.service

import android.app.Service
import android.content.Intent
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.*
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.harry1453.scavmobile.R
import com.harry1453.scavmobile.ui.MainActivity
import kotlin.random.Random


class ScavengerService : Service() {
    var scavengerProcess: Disposable? = null

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
                .subscribe({stopSelf()}, {stopSelf() /* TODO Log Error */})
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scavengerProcess?.dispose()
        scavengerProcess = null
    }

    private fun startScavenger(): Completable {
        return Completable.create{
            val filesDir = applicationContext.filesDir.absolutePath
            val outputPath = File("$filesDir/output.txt")

            if (outputPath.exists()) {
                outputPath.delete()
            }
            outputPath.createNewFile()
            val output = BufferedWriter(OutputStreamWriter(outputPath.outputStream()))

            val scavengerPath = "$filesDir/scavenger"
            val scavengerProcess = ProcessBuilder(scavengerPath)
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
            stopSelf()
        }
    }
}