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
        menu?.add(getString(R.string.menu_about))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.menu_about) -> {
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
                textView.append("""
                    Contact me:
                        E-Mail: 
                            3243430237@qq.com
                            2018hbk@gmail.com
                        Github and Gitee:
                            https://github.com/hbk01/
                            https://gitee.com/hbk01/
                            
                    This project is Open-Source on:
                        https://github.com/hbk01/Translate
                        https://gitee.com/hbk01/Translate
                        
                    We have the CLI Version:
                        https://github.com/hbk01/tr
                """.trimIndent())

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