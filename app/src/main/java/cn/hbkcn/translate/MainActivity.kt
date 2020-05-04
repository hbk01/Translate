package cn.hbkcn.translate

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.hbkcn.translate.basic.Language
import cn.hbkcn.translate.basic.Translate
import cn.hbkcn.translate.view.GenerateCard

class MainActivity : AppCompatActivity() {
    private lateinit var from: Spinner
    private lateinit var to: Spinner
    private lateinit var swap: Button
    private lateinit var editText: EditText
    private lateinit var translateBtn: Button
    private lateinit var content: LinearLayout

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initial()
    }

    @SuppressLint("InflateParams")
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
            val input = editText.text.toString()
            if (input != lastInput) {
                lastInput = input
                translate.translate(input, fromLanguage, toLanguage) {
                    runOnUiThread {
                        GenerateCard(this, layoutInflater, it).run(content)
                    }
                }
            }
        }
    }

}
