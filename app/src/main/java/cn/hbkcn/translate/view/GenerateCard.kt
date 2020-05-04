package cn.hbkcn.translate.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.hbkcn.translate.R
import cn.hbkcn.translate.basic.Response

/**
 * Generate card view.
 * @author hbk
 * @date 2020/5/4
 * @since 1.0
 */
class GenerateCard constructor(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val response: Response
) {
    private val cardList = ArrayList<View>()

    fun run(root: LinearLayout) {
        root.removeAllViews()
        cardList.clear()
        if (response.getErrorCode() == "0") {
            if (response.getExplains().isNotEmpty()) {
                genCard("基本释义", response.getExplains())
            }
            if (response.getUSPhonetic().isNotEmpty()) {
                // todo：可以在这里播放发音
                val con = LinearLayout(context)
                con.orientation = LinearLayout.VERTICAL
                val us = TextView(context)
                us.text = String.format(
                    context.getString(R.string.us_phonetic),
                    response.getUSPhonetic()
                )
                con.addView(us)
                if (response.getUKPhonetic().isNotEmpty()) {
                    val uk = TextView(context)
                    uk.text = String.format(
                        context.getString(R.string.uk_phonetic),
                        response.getUKPhonetic()
                    )
                    con.addView(uk)
                }
                genCard("音标", con)
            }
        } else {
            genCard("Error", response.getErrorCode())
        }

        // 把 list 里的卡片添加进容器
        cardList.forEach {
            root.addView(it)
        }
    }

    /**
     * 生成卡片视图
     * @param title 每个卡片都要有一个标题
     * @param content 卡片的内容
     */
    @SuppressLint("InflateParams")
    fun <T> genCard(title: String, content: T) {
        val card = inflater.inflate(R.layout.card_title, null, false)
        val titleView: TextView = card.findViewById(R.id.cardTitle)
        titleView.text = title

        val contentLayout: LinearLayout = card.findViewById(R.id.cardContent)
        var contentView: View? = TextView(context)

        when (content) {
            is String -> {
                Log.i("content", "is String.")
                contentView = TextView(context)
                contentView.text = content
            }
            is Collection<*> -> {
                Log.i("content", "is Collection.")
                contentView = TextView(context)
                content.forEach {
                    (contentView as TextView).append(it.toString())
                    (contentView as TextView).append(System.lineSeparator())
                    (contentView as TextView).append(System.lineSeparator())
                }
                // 删除最后两个空行
                contentView.text = contentView.text.removeSuffix(System.lineSeparator())
                contentView.text = contentView.text.removeSuffix(System.lineSeparator())
            }
            is Map<*, *> -> {
                Log.i("content", "is Map.")
                contentView = TextView(context)
                content.entries.forEach {
                    (contentView as TextView).append(it.key.toString() + ":" + it.value.toString())
                    (contentView as TextView).append(System.lineSeparator())
                }
            }
            is View -> {
                Log.i("content", "is View.")
                contentView = LinearLayout(context)
                contentView.addView(content)
            }
        }
        contentLayout.addView(contentView)
        cardList.add(card)
    }
}