package cn.hbkcn.translate

import android.app.Application
import android.content.Context
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

/**
 * @author hbk
 * @date 6/14/2020
 * @since 1.0
 */
class App : Application() {
    override fun onCreate() {
        app = this
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
        fun getContext(): Context {
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
            Log.i(tag, msg)
            val info: JSONObject = JSONObject().apply {
                put("level", "info")
                put("time", formatter.format(Date()))
                put("tag", tag)
                put("msg", msg)
            }
            synchronized(data) {
                data.put(info)
                save()
            }
        }

        /**
         * Log the  message as error level.
         */
        fun error(tag: String, msg: String, exception: Exception = RuntimeException("Unknown Exception.")) {
            Log.e(tag, msg)
            val error: JSONObject = JSONObject().apply {
                put("level", "error")
                put("time", formatter.format(Date()))
                put("tag", tag)
                put("throws", exception.message)
                put("msg", msg)
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
            val file = File("$logPath$fileName")
            if (file.exists()) {
                val reader = FileReader(file)
                val text = reader.readText()
                reader.close()
                if (text.isBlank()) {
                    return JSONArray("[]")
                }
                return JSONArray(text)
            }
            return JSONArray()
        }

        /**
         * Save the log data.
         */
        private fun save() {
            val file = File("$logPath$fileName")
            val writer = FileWriter(file)
            writer.write(data.toString())
            writer.flush()
            writer.close()
        }
    }
}
