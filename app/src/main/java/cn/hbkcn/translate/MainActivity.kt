package cn.hbkcn.translate

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.hbkcn.translate.basic.Language
import cn.hbkcn.translate.basic.Translate

class MainActivity : AppCompatActivity() {

    private lateinit var from: Spinner
    private lateinit var to: Spinner
    private lateinit var swap: Button
    private var swapArrow: Boolean = false

    private var fromLanguage: Language = Language.AUTO
    private var toLanguage: Language = Language.AUTO

    private var translate: Translate = Translate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initial()
        TODO("编辑框以及翻译按钮，翻译结果的显示")
    }

    private fun initial() {
        from = findViewById(R.id.language_from)
        to = findViewById(R.id.language_to)
        swap = findViewById(R.id.language_swap)

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

        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this, R.layout.spinner_item,
            lanMap.keys.toList()
        )

        from.adapter = adapter
        to.adapter = adapter

        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView: TextView = view as TextView
                textView.gravity = android.view.Gravity.CENTER_HORIZONTAL

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
                textView.gravity = android.view.Gravity.CENTER_HORIZONTAL

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
    }

}
