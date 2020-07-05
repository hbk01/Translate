package cn.hbkcn.translate.view

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.hbkcn.translate.App
import cn.hbkcn.translate.R
import cn.hbkcn.translate.basic.Errors
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
    val tag = "GengrateCard"

    /**
     * 运行生成卡片
     * @param root 卡片生成于此 view 里
     */
    fun run(root: LinearLayout) {
        root.removeAllViews()
        cardList.clear()
        if (response.getErrorCode() == "0") {
            if (response.getExplains().isNotEmpty()) {
                App.info(tag, "Explains: ${response.getExplains()}")
                genCard(getString(R.string.card_title_explains), response.getExplains())
            }

            if (response.getTranslation().isNotEmpty()) {
                App.info(tag, "Translations: ${response.getTranslation()}")
                genCard(getString(R.string.card_title_translation), response.getTranslation(),
                    onClick = View.OnClickListener {
                        playMusic(response.getToSpeakUrl())
                    })
            }

            if (response.getUSPhonetic().isNotEmpty()) {
                App.info(tag, "US Phonetic: ${response.getUSPhonetic()}")
                genCard(getString(R.string.card_title_us_speak), response.getUSPhonetic(),
                    onClick = View.OnClickListener {
                        playMusic(response.getUSPhoneticUrl())
                    })
            }

            if (response.getUKPhonetic().isNotEmpty()) {
                App.info(tag, "UK Phonetic: ${response.getUKPhonetic()}")
                genCard(getString(R.string.card_title_uk_speak), response.getUKPhonetic(),
                    onClick = View.OnClickListener {
                        playMusic(response.getUKPhoneticUrl())
                    })
            }

            if (response.getWebDict().isNotEmpty()) {
                App.info(tag, "WebDict: ${response.getWebDict()}")
                genCard(getString(R.string.card_title_webdict), response.getWebDict())
            }
        } else {
            // 查询错误码，显示错误
            App.info(tag, "Error: ${response.getErrorCode()}")
            genCard(getString(R.string.card_title_error), StringBuilder().run {
                val msg: String = Errors(response.getErrorCode()).toString()
                append(getString(R.string.error_code).format(response.getErrorCode()))
                append(System.lineSeparator())
                append(getString(R.string.error_msg).format(msg))
                toString()
            })
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
    fun <T> genCard(
        title: String, content: T,
        onClick: View.OnClickListener? = null,
        onLongClick: View.OnLongClickListener? = null
    ) {
        val card = inflater.inflate(R.layout.card_title, null, false)
        val titleView: TextView = card.findViewById(R.id.cardTitle)
        titleView.text = title

        val contentLayout: LinearLayout = card.findViewById(R.id.cardContent)
        var contentView: View? = TextView(context)

        when (content) {
            is String -> {
                App.info(tag, "add new content(type: String)")
                contentView = TextView(context)
                contentView.text = content
            }
            is Collection<*> -> {
                App.info(tag, "add new content(type: Collection)")
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
                App.info(tag, "add new content(type: Map)")
                contentView = TextView(context)
                content.forEach {
                    (contentView as TextView).append(it.toString())
                    (contentView as TextView).append(System.lineSeparator())
                    (contentView as TextView).append(System.lineSeparator())
                    contentView = TextView(context)
                    content.entries.forEach { entry ->
                        (contentView as TextView).append(entry.key.toString() + ":" + entry.value.toString())
                        (contentView as TextView).append(System.lineSeparator())
                    }
                }
            }
            is View -> {
                App.info(tag, "add new content(type: View)")
                contentView = LinearLayout(context)
                (contentView as LinearLayout).addView(content)
            }
        }
        contentLayout.addView(contentView)

        if (onClick != null) {
            card.setOnClickListener(onClick)
        }

        if (onLongClick != null) {
            card.setOnLongClickListener(onLongClick)
        }

        cardList.add(card)
    }

    /**
     * Play music form url
     * @param url music url
     */
    private fun playMusic(url: String) {
        App.info(tag, "Play Music: $url")
        val player = MediaPlayer()
        player.setDataSource(url)
        player.prepare()

        player.setOnPreparedListener { it.start() }
        player.setOnCompletionListener { it.release() }

        player.setOnErrorListener { mp, what, extra ->
            App.error(tag, "MediaPlayer.OnErrorListener", RuntimeException("$what/$extra"))
            mp?.release()
            true
        }
    }

    /**
     * Get string form strings.xml
     * @param resId resource id
     */
    private fun getString(resId: Int): String = context.getString(resId)
}