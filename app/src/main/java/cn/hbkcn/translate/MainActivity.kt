package cn.hbkcn.translate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import cn.hbkcn.translate.basic.Language
import cn.hbkcn.translate.basic.Translate
import cn.hbkcn.translate.update.Update
import cn.hbkcn.translate.view.GenerateCard
import cn.hbkcn.translate.view.LogActivity
import cn.hbkcn.translate.view.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var from: Spinner
    private lateinit var to: Spinner
    private lateinit var swap: Button
    private lateinit var editText: EditText
    private lateinit var translateBtn: Button
    private lateinit var content: LinearLayout

    /**
     * Log记录器
     */
    private lateinit var log: Log

    /**
     * 选择语言
     */
    private var swapArrow: Boolean = false
    private var fromLanguage: Language = Language.AUTO
    private var toLanguage: Language = Language.AUTO

    /**
     * 翻译
     */
    private var translate: Translate = Translate()

    /**
     * 最后地输入
     */
    private var lastInput: String = ""
    private var lastToLanguage: Language = Language.AUTO
    private var lastFromLanguage: Language = Language.AUTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        log = Log(this, "MainActivity")
        update()
        initial()
    }

    private fun update() {
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage("Checking update...")
            .setCancelable(false)
            .create()
        dialog.show()

        Update(this).checkUpdate { response ->
            // has update
            log.info("has update: $response")
            runOnUiThread {
                dialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle("New Update")
                    .setMessage(with(StringBuilder()) {
                        append("版本号：${response.versionName()}")
                        append(System.lineSeparator())
                        append("更新时间：${response.updateTime()}")
                        append(System.lineSeparator())
                        append("预览版：${if (response.preRelease()) "是" else "否"}")
                        append(System.lineSeparator())
                        append(response.body())
                        toString()
                    })
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Ignore") { _, _ ->
                        Toast.makeText(this, "Ignore", Toast.LENGTH_SHORT).show()
                    }
                    .create()
                    .show()
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun initial() {
        /**
         * 初始化控件
         */
        log.info("Initial widgets")
        from = findViewById(R.id.from)
        to = findViewById(R.id.to)
        swap = findViewById(R.id.swap)
        editText = findViewById(R.id.editText)
        translateBtn = findViewById(R.id.translateBtn)
        content = findViewById(R.id.content)

        /**
         * 初始化适配器
         */
        log.info("Initial language adapter")
        val lanMap: LinkedHashMap<String, String> = LinkedHashMap()
        with(lanMap) {
            put(getString(R.string.lan_auto), "auto")
            put(getString(R.string.lan_zh), "zh-CHS")
            put(getString(R.string.lan_en), "en")
            put(getString(R.string.lan_ja), "ja")
            put(getString(R.string.lan_ko), "ko")
            put(getString(R.string.lan_fr), "fr")
            put(getString(R.string.lan_ru), "ru")
            put(getString(R.string.lan_de), "de")
        }

        log.info("Set language adapter to widgets.")
        val data = lanMap.keys.toList()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, data)
        from.adapter = adapter
        to.adapter = adapter

        log.info("Add first card.")
        val cardView = layoutInflater.inflate(R.layout.card_title, null)
        val cardTitle: TextView = cardView.findViewById(R.id.cardTitle)
        cardTitle.append(getString(R.string.default_tip))
        val divider: View = cardView.findViewById(R.id.divider)
        divider.visibility = View.GONE
        content.addView(cardView)


        /**
         * 初始化监听器
         */
        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView: TextView = view as TextView
                textView.gravity = android.view.Gravity.CENTER

                val code = lanMap.getValue(textView.text.toString())
                if (swapArrow) {
                    toLanguage = Language.getLanguage(code)
                } else {
                    fromLanguage = Language.getLanguage(code)
                }
            }
        }

        to.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView: TextView = view as TextView
                textView.gravity = android.view.Gravity.CENTER

                val code = lanMap.getValue(textView.text.toString())

                if (swapArrow) {
                    fromLanguage = Language.getLanguage(code)
                } else {
                    toLanguage = Language.getLanguage(code)
                }
            }
        }

        swap.setOnClickListener {
            swapArrow = !swapArrow

            // swap background image
            if (swapArrow) {
                swap.setBackgroundResource(R.drawable.ic_chevron_left_black_24dp)
            } else {
                swap.setBackgroundResource(R.drawable.ic_chevron_right_black_24dp)
            }

            // swap language
            val temp = fromLanguage
            fromLanguage = toLanguage
            toLanguage = temp
        }

        translateBtn.setOnClickListener {
            log.info("Translate button clicked.")
            val input = editText.text.toString()
            if (lastInput != input ||
                lastToLanguage != toLanguage ||
                lastFromLanguage != fromLanguage
            ) { // 只要有一个变动就进行翻译操作
                lastInput = input
                lastToLanguage = toLanguage
                lastFromLanguage = fromLanguage

                // added at v2.0.1, remove all views and add progress.
                content.removeAllViews()
                val progress = ProgressBar(this)
                content.addView(progress)

                log.info("Translate: %s, Language: %s-%s".format(input, fromLanguage.code, toLanguage.code))
                translate.translate(this, input, fromLanguage, toLanguage) {
                    runOnUiThread {
                        GenerateCard(this, layoutInflater, it).run(content)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(R.string.menu_settings)
        menu?.add(R.string.menu_about)
        val preference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val logMode: Boolean = preference.getBoolean(getString(R.string.preference_key_log), false)
        if (logMode) {
            menu?.add(R.string.preference_catalog_log)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.menu_settings) -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            getString(R.string.menu_about) -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.menu_about)
                    .setMessage(
                        getString(R.string.about_msg).format(
                            getString(R.string.app_name),
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        )
                    )
                    .setPositiveButton(R.string.dialog_ok, null)
                    .create()
                    .show()
            }
            getString(R.string.preference_catalog_log) -> {
                startActivity(Intent(this, LogActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
