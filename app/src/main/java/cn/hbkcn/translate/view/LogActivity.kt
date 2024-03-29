package cn.hbkcn.translate.view

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.hbkcn.translate.App
import cn.hbkcn.translate.R
import org.json.JSONArray
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class LogActivity : AppCompatActivity() {
    private val defaultFormat = "%time %level %tag %msg %throws"
    private var array: JSONArray = App.readTodayLog()
    private val temp: JSONArray = JSONArray()
    private lateinit var logText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        logText = findViewById(R.id.logText)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
            .format(Date()) + ".log"
        showLog()
    }

    /**
     * Show the log form array
     */
    private fun showLog() {
        // clear text first.
        logText.text = ""

        var format: String = App.getSettings().getString(
            getString(R.string.preference_key_log_format), defaultFormat
        ).toString()
        format = if (format == "") defaultFormat else format

        (0 until array.length()).forEach {
            val obj = array.getJSONObject(it)
            val time = obj.getString("time")
            val tag = obj.getString("tag")
            val msg = obj.getString("msg")
            val level = obj.getString("level")
            val throws = obj.getJSONArray("throws")

            val limit = App.getSettings().getInt(
                getString(R.string.preference_key_log_error_limit), 3
            )
            val builder = StringBuilder()
            val throwsMsg = if (limit == 0) {
                (0 until throws.length()).forEach { index ->
                    builder.appendLine(throws[index])
                }
                builder.toString()
            } else {
                // 考虑到 throws 长度小于指定的长度的情况，要按照 throws 的长度显示
                val line = if (throws.length() < limit) throws.length() else limit
                (0 until line).forEach { index: Int ->
                    builder.appendLine(throws[index])
                }
                builder.toString()
            }

            val str = format.replace("%time", time)
                .replace("%level", level)
                .replace("%tag", tag)
                .replace("%msg", msg)
                .replace("%throws", System.lineSeparator() + throwsMsg)

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

    /**
     *
     * @param name tag name
     * @param text filter text
     * @param isLike match method
     */
    private fun JSONArray.filter(name: String, text: String, isLike: Boolean = false) {
        // clear temp array first.
        temp.clear()

        // if not have input, we skip it.
        if (name.isEmpty() || text.isEmpty()) {
            return
        }

        // add item to temp array.
        (0 until length()).forEach {
            val obj = this.getJSONObject(it)
            if (isLike) {
                val a = obj.getString(name).lowercase(Locale.CHINA)
                val b = text.lowercase(Locale.CHINA)
                if (a.contains(b)) {
                    temp.put(obj)
                }
            } else {
                if (obj.getString(name) == text) {
                    temp.put(obj)
                }
            }
        }

        // remove all item
        this.clear()

        // add all json object to this array form temp.
        this.copy(temp)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(R.string.menu_log_history)
        menu?.add(R.string.menu_log_filter)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.menu_log_filter) -> {
                val holder = FilterHolder(this)
                val builder = with(AlertDialog.Builder(this)) {
                    setCancelable(false)
                    setView(holder.layout)
                    setPositiveButton(R.string.dialog_ok) { _, _ ->
                        array.filter("level", holder.level.text.toString())
                        array.filter("tag", holder.tag.text.toString())
                        array.filter("msg", holder.msg.text.toString(), true)
                        array.filter("time", holder.time.text.toString(), true)
                        showLog()
                    }
                    setNegativeButton(R.string.dialog_cancel, null)
                    setNeutralButton(R.string.dialog_reset) { _, _ ->
                        // remove all
                        array.clear()
                        // read the full log into array
                        array.copy(App.readTodayLog())
                        showLog()
                    }
                }
                builder.show()
            }
            getString(R.string.menu_log_history) -> {
                val allLog = App.listAllLog()
                allLog.sort()
                AlertDialog.Builder(this)
                    .setItems(allLog) { _, index ->
                        supportActionBar?.subtitle = allLog[index]
                        array = App.readLog(allLog[index])
                        showLog()
                    }
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class FilterHolder(context: Context) {
        val layout = LinearLayout(context)
        val level = EditText(context)
        val tag = EditText(context)
        val msg = EditText(context)
        val time = EditText(context)

        init {
            layout.orientation = LinearLayout.VERTICAL
            level.hint = "level (equal)"
            tag.hint = "tag (equal)"
            msg.hint = "msg (like)"
            time.hint = "time (like)"

            layout.addView(level)
            layout.addView(tag)
            layout.addView(msg)
            layout.addView(time)
        }
    }
}

/**
 * Copy other JSONArray into this JSONArray.
 */
private fun JSONArray.copy(other: JSONArray) {
    (0 until other.length()).forEach {
        put(other.get(it))
    }
}

/**
 * Clear the json array.
 */
private fun JSONArray.clear() {
    (0 until length()).forEach { _ ->
        remove(0)
    }
}

