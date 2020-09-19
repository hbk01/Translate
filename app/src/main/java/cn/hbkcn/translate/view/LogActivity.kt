package cn.hbkcn.translate.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.hbkcn.translate.App
import cn.hbkcn.translate.R
import kotlinx.android.synthetic.main.activity_log.*

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        val array = App.readTodayLog()

        val defaultFormat = "%time %tag %level %msg %throws"
        var format: String = App.getSettings().getString(
            getString(R.string.preference_key_log_format), defaultFormat
        ).toString()

        if (format == "") {
            format = defaultFormat
        }

        (0 until array.length()).forEach {
            val obj = array.getJSONObject(it)
            val time = obj.getString("time")
            val tag = obj.getString("tag")
            val msg = obj.getString("msg")
            val level = obj.getString("level")

            val str = format.replace("%time", time)
                .replace("%tag", tag)
                .replace("%msg", msg)
                .replace("%level", level)

            if (level == "error") {
                val throws = obj.getString("throws")
                logText.append(str.replace("%throws", throws))
            } else {
                logText.append(str.replace("%throws", ""))
            }
            logText.append(System.lineSeparator())
            logText.append(System.lineSeparator())
        }
    }
}