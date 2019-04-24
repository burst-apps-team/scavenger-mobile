package com.harry1453.scavmobile.ui

import android.app.Application
import android.os.FileObserver
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.StringWriter

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val logFile: MutableLiveData<String> = MutableLiveData()
    var observer: FileObserver? = null

    fun watchFile() {
        if (observer == null) {
            val pathToWatch = getApplication<Application>().getExternalFilesDir(null)!!.absolutePath
            val observer = object : FileObserver(pathToWatch, FileObserver.MODIFY) {
                override fun onEvent(event: Int, filePath: String?) {
                    if (filePath?.equals("output.txt") == true) {
                        val file = File(pathToWatch, filePath)
                        val reader = BufferedReader(FileReader(file))
                        val writer = StringWriter()
                        reader.copyTo(writer)
                        reader.close()
                        writer.close()
                        logFile.postValue(writer.toString())
                    }
                }
            }
            observer.startWatching()
            this.observer = observer
        }
    }

    fun getLogFile() : LiveData<String> = logFile
}