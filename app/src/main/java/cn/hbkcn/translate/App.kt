package cn.hbkcn.translate

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import cn.hbkcn.translate.databases.DatabaseHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author hbk
 * @date 6/14/2020
 * @since 1.0
 */
class App : Application() {
    override fun onCreate() {
        app = this
        ExceptionHandler().install()
        preference = PreferenceManager.getDefaultSharedPreferences(this)
        db = DatabaseHelper(this)
        logPath = if (getContext().cacheDir.absolutePath.endsWith("/")) {
            getContext().cacheDir.absolutePath
        } else {
            "${getContext().cacheDir.absolutePath}/"
        }
        data = readTodayLog()
        super.onCreate()
    }

    companion object {
        private lateinit var app: App
        private lateinit var preference: SharedPreferences
        private lateinit var db: DatabaseHelper

        private lateinit var logPath: String
        private lateinit var data: JSONArray
        private val formatter = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
        private val fileName = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date()) + ".log"

        /**
         * Get context in anywhere.
         * @return context.
         * @author hbk01
         */
        fun getContext(): Application {
            return app
        }

        /**
         * Get Database Helper
         */
        fun getDatabaseHelper(): DatabaseHelper {
            return db
        }

        /**
         * Get settings in anywhere.
         * @return SharedPreference
         */
        fun getSettings(): SharedPreferences {
            return preference
        }

        /**
         * Log the message as info level.
         */
        fun info(tag: String, msg: String) {
            Log.i(tag, msg.replace("\\", ""))
            val info: JSONObject = JSONObject().apply {
                put("level", "info")
                put("time", formatter.format(Date()))
                put("tag", tag)
                put("msg", msg.replace("\\", ""))
                put("throws", JSONArray())
            }
            synchronized(data) {
                data.put(info)
                save()
            }
        }

        /**
         * Log the  message as error level.
         */
        fun error(
            tag: String, msg: String,
            exception: Throwable = RuntimeException("Unknown Exception")
        ) {
            Log.e(tag, msg, exception)
            val error: JSONObject = JSONObject().apply {
                put("level", "error")
                put("time", formatter.format(Date()))
                put("tag", tag)
                put("msg", msg)
                put("throws", JSONArray().apply {
                    exception.stackTrace.forEach {
                        put("at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
                    }
                })
            }
            synchronized(data) {
                data.put(error)
                save()
            }
        }

        /**
         * Read today log file.
         * @return JSONArray
         * @author hbk01
         */
        fun readTodayLog(): JSONArray {
            return readLog(fileName)
        }

        /**
         * Read the log of the specified date.
         * @return date. format is 'yyyyMMdd.log', for example: '20210325.log'
         */
        fun readLog(date: String): JSONArray {
            val file = File("$logPath$date")
            if (file.exists()) {
                val reader = FileReader(file)
                val text = reader.readText()
                reader.close()
                if (text.isBlank()) {
                    return JSONArray("[]")
                }
                return JSONArray(text)
            }
            return JSONArray("[]")
        }

        /**
         * List all log file name.
         * @return all filename. if not have anything, return an empty array.
         */
        fun listAllLog(): Array<out String> {
            val files = File(logPath).listFiles()
            val list: MutableList<String> = ArrayList()
            return if (files != null) {
                files.iterator().forEach {
                    if (it.isFile && it.name.endsWith(".log")) {
                        list.add(it.name)
                    }
                }
                list.toTypedArray()
            } else {
                emptyArray()
            }
        }

        /**
         * Save the log data.
         */
        private fun save() {
            val file = File("$logPath$fileName")
            val writer = FileWriter(file)
            writer.write(data.toString(4))
            writer.flush()
            writer.close()
        }
    }
}
