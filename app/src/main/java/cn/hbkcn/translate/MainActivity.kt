package cn.hbkcn.translate

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.hbkcn.translate.basic.Language
import cn.hbkcn.translate.basic.Translate

class MainActivity : AppCompatActivity() {
    /**
     * 选择语言
     */
    private lateinit var from: Spinner
    private lateinit var to: Spinner
    private lateinit var swap: Button
    private var swapArrow: Boolean = false
    private var fromLanguage: Language = Language.AUTO
    private var toLanguage: Language = Language.AUTO

    /**
     * 输入及翻译
     */
    private lateinit var editText: EditText
    private lateinit var trans: Button
    private var translate: Translate = Translate()

    /**
     * 最后地输入
     */
    private var lastInput: String = ""

    /**
     * 结果显示
     */
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initial()
    }

    private fun initial() {
        /**
         * 初始化控件
         */
        from = findViewById(R.id.language_from)
        to = findViewById(R.id.language_to)
        swap = findViewById(R.id.language_swap)
        editText = findViewById(R.id.main_editText)
        trans = findViewById(R.id.main_translateBtn)
        content = findViewById(R.id.main_content)

        /**
         * 初始化适配器
         */
        val lanMap: LinkedHashMap<String, String> = LinkedHashMap()
        with(lanMap) {
            put("自动识别", "auto")
            put("中文", "zh-CHS")
            put("英文", "en")
            put("日文", "ja")
            put("韩文", "ko")
            put("法文", "fr")
            put("俄文", "ru")
            put("德文", "de")
        }

        val data = lanMap.keys.toList()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, data)
        from.adapter = adapter
        to.adapter = adapter

        val cardView = layoutInflater.inflate(R.layout.card_title, null)
        val title: TextView = cardView.findViewById(R.id.card_title)
        title.text = getString(R.string.default_tip)
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

        trans.setOnClickListener {
            val input = editText.text.toString()
            if (input != lastInput) {
                lastInput = input
                translate.translate(input, fromLanguage, toLanguage) {
                    runOnUiThread {
                        content.removeAllViews()
                        val child = layoutInflater.inflate(R.layout.card_title, null)
                        if (it.getErrorCode() == "0") {
                            TODO("建个类来弄这些，不然要累死")
                            val title: TextView = child.findViewById(R.id.card_title)
                            val contentLayout: LinearLayout = child.findViewById(R.id.card_content)
                            val contentText = TextView(this)
                            it.getExplains().forEach {
                                contentText.append(it)
                                contentText.append(System.lineSeparator())
                            }
                            title.text = "基本释义"
                            contentLayout.addView(contentText)
                            content.addView(child)
                        } else {
                            val title: TextView = child.findViewById(R.id.card_title)
                            title.text = "Error: " + it.getErrorCode()
                            content.addView(child)
                        }
                    }
                }
            }
        }
    }

}
