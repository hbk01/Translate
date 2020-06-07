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
        val log = Log(this, "LogActivity").read()
        // TODO: 解决因array对象生命周期问题导致的数据被重写问题
        val array = JSONArray(log)
        val format = "%s :: %s :: %s"

        (0 until array.length()).forEach {
            val obj = array.getJSONObject(it)
            val time = obj.getString("time")
            val tag = obj.getString("tag")
            val msg = obj.getString("msg")
            if (obj.has("throws")) {
                val throws = obj.getString("throws")
                logText.append(format.format(time, tag, msg) + " :: " + throws)
            }
            logText.append(format.format(time, tag, msg))
            logText.append(System.lineSeparator())
        }
    }
}