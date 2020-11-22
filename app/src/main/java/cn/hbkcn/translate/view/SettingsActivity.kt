package cn.hbkcn.translate.view

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import cn.hbkcn.translate.BuildConfig
import cn.hbkcn.translate.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("About")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            "About" -> {
                val layout = LinearLayout(this)
                layout.setPadding(10, 10, 10, 10)
                val textView = TextView(this)
                textView.textSize = 14f
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    textView.setTextColor(getColor(R.color.colorAccent))
                } else {
                    textView.setTextColor(Color.BLACK)
                }
                textView.setPadding(10, 10, 10, 10)
                textView.autoLinkMask = Linkify.ALL
                textView.append("Translate v${BuildConfig.VERSION_NAME} build by hbk01.\n\n")
                textView.append("Contact me:\n")
                textView.append("    E-Mail: 3243430237@qq.com\n")
                textView.append("    Github: https://github.com/hbk01/\n")
                textView.append("    Gitee : https://gitee.com/hbk01/\n\n")
                textView.append("This project is Open-Source on:\n")
                textView.append("    https://github.com/hbk01/Translate\n")
                textView.append("    https://gitee.com/hbk01/Translate\n\n")
                textView.append("We have the CLI Version for command line:\n")
                textView.append("    https://github.com/hbk01/tr")

                layout.addView(textView)
                AlertDialog.Builder(this)
                    .setView(layout)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}