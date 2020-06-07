package cn.hbkcn.translate

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author hbk
 * @date 6/7/2020
 * @since 1.0
 */
class Log(context: Context, private val tag: String) {
    private val dateTimeFormat = "YYYY-MM-dd HH:mm:ss.SSS"
    private val formatter = SimpleDateFormat(dateTimeFormat, Locale.CHINA)
    private val data = JSONArray()
    private val logPath = "%s%s%s.log".format(
        context.externalCacheDir?.absolutePath,
        File.separator,
        SimpleDateFormat("YYYYMMdd", Locale.CHINA).format(Date())
    )

    fun info(msg: String) {
        val info: JSONObject = JSONObject().apply {
            put("time", formatter.format(Date()))
            put("tag", tag)
            put("msg", msg)
        }
        data.put(info)
        write()
    }

    fun error(msg: String, throws: Throwable) {
        val info: JSONObject = JSONObject().apply {
            put("time", formatter.format(Date()))
            put("tag", tag)
            put("msg", msg)
            put("throw", throws.message)
        }
        data.put(info)
        write()
    }

    private fun write() {
        val writer = FileWriter(File(logPath))
        writer.write(data.toString())
        writer.flush()
        writer.close()
    }

    fun read(): String {
        val lines = FileReader(File(logPath)).readLines()
        return with(StringBuilder()) {
            lines.forEach {
                append(it)
            }
            toString()
        }
    }
}