package cn.hbkcn.translate.view

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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
            val throws = obj.optString("throws", "")

            val str = format.replace("%time", time)
                .replace("%tag", tag)
                .replace("%msg", msg)
                .replace("%level", level)
                .replace("%throws", throws)

            if (level == "error") {
                val span = SpannableStringBuilder(str)
                span.setSpan(
                    ForegroundColorSpan(Color.RED),
                    0, str.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                logText.append(span)
            } else {
                logText.append(str)
            }
            logText.append(System.lineSeparator())
            logText.append(System.lineSeparator())
        }
    }
}