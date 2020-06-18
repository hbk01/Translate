package cn.hbkcn.translate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
    private val log = Log()

    /**
     * 获取设置
     */
    private lateinit var preference: SharedPreferences

    /**
     * 选择语言
     */
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
        preference = PreferenceManager.getDefaultSharedPreferences(this)
        if (preference.getBoolean(getString(R.string.preference_key_update), true)) {
            update()
        }
        initial()
    }

    private fun update() {
        log.info(javaClass, "Checking update...")
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage(R.string.dialog_checking_update)
            .create()
        dialog.show()

        Update(this).checkUpdate { response ->
            val code = response.versionCode()
            if (code > BuildConfig.VERSION_CODE) {
                runOnUiThread {
                    log.info(javaClass, "Has update: $response")
                    dialog.dismiss()
                    AlertDialog.Builder(this)
                        .setMessage(with(StringBuilder()) {
                            append(getString(R.string.update_version_name).format(BuildConfig.VERSION_NAME, response.versionName()))
                            append(System.lineSeparator())
                            append(getString(R.string.update_version_code).format(BuildConfig.VERSION_CODE, response.versionCode()))
                            append(System.lineSeparator())
                            append(getString(R.string.update_time).format(response.updateTime()))
                            append(System.lineSeparator())
                            val isPreRelease =
                                if (response.preRelease())
                                    getString(R.string.update_true)
                                else
                                    getString(R.string.update_false)
                            append(getString(R.string.update_pre_release).format(isPreRelease))
                            append(System.lineSeparator())
                            append(getString(R.string.update_change_log))
                            append(System.lineSeparator())
                            append(response.body())
                            toString()
                        })
                        .setPositiveButton(R.string.update) { _, _ ->
                            // 码云要登录才能下载文件（辣鸡），改用 Github 下载地址
                            val url = "https://github.com/hbk01/Translate/releases/download/" +
                                    "${response.versionName()}/${response.apkName()}"
                            log.info(javaClass, "Download update: $url")
                            Update(this).download(url, response.apkName())
                        }
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .setNeutralButton(R.string.update_website_download) { _, _ ->
                            // 跳转到浏览器，打开下载页面
                            AlertDialog.Builder(this)
                                .setMessage(with(java.lang.StringBuilder()) {
                                    append("由于码云下载文件需要登录，所以默认使用的是 github 下载，")
                                    append("而 github 在国内处于半墙状态，下载很不稳定，")
                                    append("所以也提供了码云的下载方式，不过你需要自行登录码云才能开始下载。")
                                    append(System.lineSeparator())
                                    append("点击确定将会打开码云的下载链接，在登录码云后会自动开始下载更新包。")
                                    append(System.lineSeparator())
                                    append(System.lineSeparator())
                                    append("最后说一句，码云真好。")
                                    toString()
                                })
                                .setPositiveButton(R.string.dialog_ok) { _, _ ->
                                    log.info(javaClass, "Open url: ${response.apkUrl()}")
                                    val uri = Uri.parse(response.apkUrl())
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show()
                        }
                        .create()
                        .show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, R.string.update_no_update, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initial() {
        /**
         * 初始化控件
         */
        log.info(javaClass, "Initial widgets")
        from = findViewById(R.id.from)
        to = findViewById(R.id.to)
        swap = findViewById(R.id.swap)
        editText = findViewById(R.id.editText)
        translateBtn = findViewById(R.id.translateBtn)
        content = findViewById(R.id.content)

        /**
         * 初始化适配器
         */
        log.info(javaClass, "Initial language adapter")
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

        log.info(javaClass, "Set language adapter to widgets.")
        val data = lanMap.keys.toList()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, data)
        from.adapter = adapter
        to.adapter = adapter

        log.info(javaClass, "Add first card.")
        val cardView = layoutInflater.inflate(R.layout.card_title, null)
        val cardTitle: TextView = cardView.findViewById(R.id.cardTitle)
        cardTitle.append(getString(R.string.default_tip))
        val divider: View = cardView.findViewById(R.id.divider)
        divider.visibility = View.GONE
        content.addView(cardView)


        /**
         * 初始化监听器
         */
        log.info(javaClass, "Initial widgets listener.")
        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView: TextView = view as TextView
                textView.gravity = android.view.Gravity.CENTER
                val code = lanMap.getValue(textView.text.toString())
                fromLanguage = Language.getLanguage(code)
            }
        }

        to.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView: TextView = view as TextView
                textView.gravity = android.view.Gravity.CENTER
                val code = lanMap.getValue(textView.text.toString())
                toLanguage = Language.getLanguage(code)
            }
        }

        translateBtn.setOnClickListener {
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

                val msg = "Translate: %s, Language: %s-%s"
                log.info(javaClass, msg.format(input, fromLanguage.code, toLanguage.code))
                translate.translate(input, fromLanguage, toLanguage) {
                    log.info(javaClass, it.toString())
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

        if (preference.getBoolean(getString(R.string.preference_key_log), false)) {
            menu?.add(R.string.preference_catalog_log)
        }

        // 如果关闭了自动检查更新，则开启手动更新
        if (!preference.getBoolean(getString(R.string.preference_key_update), true)) {
            menu?.add(R.string.preference_title_update)
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
            getString(R.string.preference_title_update) -> {
                update()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
