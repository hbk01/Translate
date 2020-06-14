package cn.hbkcn.translate

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author hbk
 * @date 6/7/2020
 * @since 1.0
 */
class Log {
    private val formatter = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    private val path = App.getContext().externalCacheDir?.absolutePath.toString()
    private val fileName = SimpleDateFormat("YYYYMMdd", Locale.CHINA).format(Date()) + ".log"
    private val data = JSONArray()

    fun info(clazz: Class<Any>, msg: String) {
        Log.i(clazz.simpleName, msg)
        val info: JSONObject = JSONObject().apply {
            put("level", "info")
            put("time", formatter.format(Date()))
            put("tag", clazz.simpleName)
            put("msg", msg)
        }
        data.put(info)
        save()
    }

    fun error(clazz: Class<Any>, msg: String, throws: Throwable) {
        Log.e(clazz.simpleName, msg, throws)
        val info: JSONObject = JSONObject().apply {
            put("level", "error")
            put("time", formatter.format(Date()))
            put("tag", clazz.simpleName)
            put("msg", msg)
            put("throw", throws.message)
        }
        data.put(info)
        save()
    }

    private fun save() {
        val writer = FileWriter("$path/$fileName")
        writer.appendln(data.toString())
        writer.flush()
        writer.close()
    }

    fun read(): String {
        val reader = FileReader("$path/$fileName")
        val text = reader.readText()
        reader.close()
        return text
    }
}