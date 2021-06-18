package cn.hbkcn.translate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

class MainActivity : AppCompatActivity() {
    private lateinit var from: Spinner
    private lateinit var to: Spinner
    private lateinit var swap: Button
    private lateinit var editText: EditText
    private lateinit var translateBtn: Button
    private lateinit var content: LinearLayout

    /**
     * Log tag
     */
    private val tag = "MainActivity"

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
    private val translate: Translate = Translate()

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

        // init application widgets
        initial()

        // 翻译通过分享传入的内容
        if (Intent.ACTION_SEND == intent.action) {
            when (intent.type) {
                "text/plain" -> {
                    val text: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
                    App.info(tag, JSONObject().apply {
                        put("package", intent.`package`)
                        put("action", intent.action)
                        put("text", text)
                    }.toString(4))
                    if (text != null && text.isNotBlank()) {
                        editText.setText(text)
                        translateBtn.callOnClick()
                    }
                }
            }
        } else if (preference.getBoolean(getString(R.string.preference_key_update), true)) {
            Update(this).update()
        }

    }

    override fun onResume() {
        // 自动读取剪切板内容
        if (preference.getBoolean(getString(R.string.preference_key_clipboard), false)) {
            putClipboardDataToEditor()
        }
        super.onResume()
    }

    /**
     * Get the clipboard data when starting the application.
     * @return clipboard string.
     */
    private fun putClipboardDataToEditor(): String {
        // Get clipboard data.
        window.decorView.post {
            val manager: ClipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (manager.hasPrimaryClip() && manager.primaryClip!!.itemCount > 0) {
                val text = manager.primaryClip!!.getItemAt(0).text.toString()
                this.editText.setText(text)
            }
        }
        return ""
    }

    /**
     * Get yiyan form web
     * @param callback callback
     */
    private fun yiYan(callback: (String) -> Unit) {
        Thread {
            val url = "https://v1.hitokoto.cn?encode=text&charset=utf-8"
            val client = OkHttpClient()
            val req: Request = Request.Builder().url(url).get().build()
            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    App.error(tag, "Get YiYan Field.", e)
                    runOnUiThread {
                        callback.invoke(getString(R.string.default_tip))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val yiYan = response.body?.string() ?: getString(R.string.default_tip)
                    App.info(tag, JSONObject().apply {
                        put("url", call.request().url)
                        put("respCode", response.code)
                        put("respMsg", response.message)
                        put("respBody", yiYan)
                    }.toString(4))
                    runOnUiThread { callback.invoke(yiYan) }
                }
            })
        }.start()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility", "SimpleDateFormat", "ResourceType", "UseCompatLoadingForDrawables")
    private fun initial() {
        /**
         * 初始化控件
         */
        from = findViewById(R.id.from)
        to = findViewById(R.id.to)
        swap = findViewById(R.id.swap)
        editText = findViewById(R.id.editText)
        translateBtn = findViewById(R.id.translateBtn)
        content = findViewById(R.id.content)

        /**
         * 初始化适配器
         */
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

        val data = lanMap.keys.toList()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, data)
        from.adapter = adapter
        to.adapter = adapter

        val cardView = layoutInflater.inflate(R.layout.card_title, null)
        val cardTitle: TextView = cardView.findViewById(R.id.cardTitle)
        cardTitle.append(getString(R.string.default_tip))
        cardView.setOnClickListener {
            yiYan { cardTitle.text = it }
        }

        // YiYan
        if (preference.getBoolean(getString(R.string.preference_key_yi_yan), false)) yiYan { cardTitle.text = it }

        // clear divider in card view.
        val divider: View = cardView.findViewById(R.id.divider)
        divider.visibility = View.GONE
        content.addView(cardView)

        // 填充历史记录
        if (preference.getBoolean(getString(R.string.preference_key_translate_history), true)) {
            val json = App.getDatabaseHelper().selectHistory(5)
            json.forEach { (k, v) ->
                val response = cn.hbkcn.translate.basic.Response(v)
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(k))

                // get widgets.
                val historyCard = layoutInflater.inflate(R.layout.card_title, null)
                historyCard.setOnClickListener {
                    GenerateCard(this, layoutInflater, response).run(content)
                }
                val title: TextView = historyCard.findViewById(R.id.cardTitle)

                val contentLayout: LinearLayout = historyCard.findViewById(R.id.cardContent)
                val value = TextView(this)

                val timeValue = TextView(this)
                timeValue.append(System.lineSeparator())
                timeValue.append(time)


                title.text = response.getQuery()
                response.getTranslation().forEach {
                    value.append(it)
                    value.append(System.lineSeparator())
                }
                value.text = value.text.removeSuffix(System.lineSeparator())

                contentLayout.addView(value)
                contentLayout.addView(timeValue)
                content.addView(historyCard)
            }
        }

        /**
         * 初始化监听器
         */
        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 切换深色模式时 view 会为 null，所以这里不能用 view， 只能用 parent
                val code = lanMap.getValue(parent?.selectedItem.toString())
                fromLanguage = Language.getLanguage(code)
            }
        }

        to.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val code = lanMap.getValue(parent?.selectedItem.toString())
                toLanguage = Language.getLanguage(code)
            }
        }

        var count = 0
        val handle = Handler(Looper.myLooper()!!)
        translateBtn.setOnClickListener {
            count++
            handle.postDelayed({
                when (count) {
                    1 -> {
                        // single click
                        Log.e("onTouch", "onClick")
                        val input = editText.text.toString()
                        if (input != "" || lastInput != input ||
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

                            App.info(tag, JSONObject().apply {
                                put("translate", input)
                                put("fromLanguage", fromLanguage.code)
                                put("toLanguage", toLanguage.code)
                            }.toString(4))
                            translate.translate(input, fromLanguage, toLanguage) {
                                if (it.getErrorCode() != "100") {
                                    App.getDatabaseHelper().insertHistory(it.toString())
                                }
                                runOnUiThread {
                                    GenerateCard(this, layoutInflater, it).run(content)
                                }
                            }
                        }
                    }
                    2 -> {
                        // double click
                        Log.e("onTouch", "onDoubleClick")
                        if (App.getSettings().getBoolean(getString(R.string.preference_key_double_click_paste), true)) {
                            putClipboardDataToEditor()
                        }
                    }
                }
                count = 0
                handle.removeCallbacksAndMessages(null)
            }, 250)
        }

        // Long click translate button to paste text.
        translateBtn.setOnLongClickListener {
            editText.setText("")
            return@setOnLongClickListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(R.string.menu_settings)
        menu?.add(R.string.menu_problem)
        menu?.add(R.string.feedback)

        if (preference.getBoolean(getString(R.string.preference_key_log), false)) {
            menu?.add(R.string.preference_catalog_log)
        }

        // 如果关闭了自动检查更新，则开启手动更新
        if (!preference.getBoolean(getString(R.string.preference_key_update), true)) {
            menu?.add(R.string.preference_title_update)
        }
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.menu_settings) -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            getString(R.string.menu_problem) -> {
                val url = "https://gitee.com/hbk01/Translate/blob/master/answer.md"
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(url)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(intent, "Select Browser"))
                } else {
                    startActivity(intent)
                }
            }
            getString(R.string.preference_catalog_log) -> {
                startActivity(Intent(this, LogActivity::class.java))
            }
            getString(R.string.preference_title_update) -> Update(this).update()
            getString(R.string.feedback) -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.feedback)
                        .setMessage(R.string.feedback_tips)
                        .setPositiveButton(R.string.feedback_gitee_btn) { _, _ ->
                            val url = "https://gitee.com/hbk01/Translate/issues"
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        .setNegativeButton(R.string.feedback_github_btn) { _, _ ->
                            val url = "https://github.com/hbk01/Translate/issues"
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        .create()
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
