package cn.hbkcn.translate.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.hbkcn.translate.Log
import cn.hbkcn.translate.R
import kotlinx.android.synthetic.main.activity_log.*
import org.json.JSONArray

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        val array = JSONArray(Log().read())
        val format = "%s :: %s :: %s"

        (0 until array.length()).forEach {
            val obj = array.getJSONObject(it)
            val time = obj.getString("time")
            val tag = obj.getString("tag")
            val msg = obj.getString("msg")
            val level = obj.getString("level")
            if (level == "error") {
                val throws = obj.getString("throws")
                logText.append(format.format(time, tag, msg) + " :: " + throws)
            } else {
                logText.append(format.format(time, tag, msg))
            }
            logText.append(System.lineSeparator())
        }
    }
}