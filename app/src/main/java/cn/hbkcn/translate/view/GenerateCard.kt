package cn.hbkcn.translate.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.hbkcn.translate.App
import cn.hbkcn.translate.R
import cn.hbkcn.translate.basic.Errors
import cn.hbkcn.translate.basic.Response
import org.json.JSONObject

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
    private val tag = "GenerateCard"

    /**
     * 运行生成卡片
     * @param root 卡片生成于此 view 里
     */
    fun run(root: LinearLayout) {
        root.removeAllViews()
        cardList.clear()
        if (response.getErrorCode() == "0") {
            val json = JSONObject()
            if (response.getExplains().isNotEmpty()) {
                json.put("explains", response.getExplains())
                genCard(getString(R.string.card_title_explains), response.getExplains())
            }

            if (response.getTranslation().isNotEmpty()) {
                json.put("translations", response.getTranslation())
                genCard(getString(R.string.card_title_translation), response.getTranslation(),
                    onClick = {
                        playMusic(response.getToSpeakUrl())
                    })
            }

            if (response.getUSPhonetic().isNotEmpty()) {
                json.put("US-phonetic", response.getUSPhonetic())
                genCard(getString(R.string.card_title_us_speak), response.getUSPhonetic(),
                    onClick = {
                        playMusic(response.getUSPhoneticUrl())
                    })
            }

            if (response.getUKPhonetic().isNotEmpty()) {
                json.put("UK-phonetic", response.getUKPhonetic())
                genCard(getString(R.string.card_title_uk_speak), response.getUKPhonetic(),
                    onClick = {
                        playMusic(response.getUKPhoneticUrl())
                    })
            }

            if (response.getWebDict().isNotEmpty()) {
                json.put("webdict", response.getWebDict())
                genCard(getString(R.string.card_title_webdict), response.getWebDict())
            }

            App.info(tag, json.toString(4))
        } else {
            // 查询错误码，显示错误
            App.error(
                tag, "Error: ${response.getErrorCode()}",
                RuntimeException("ERROR_CODE=${response.getErrorCode()}")
            )
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
        onClick: View.OnClickListener? = null
    ) {
        val card = inflater.inflate(R.layout.card_title, null, false)
        val titleView: TextView = card.findViewById(R.id.cardTitle)
        titleView.text = title

        val contentLayout: LinearLayout = card.findViewById(R.id.cardContent)
        val contentView = TextView(context)
        contentView.tag = "content"

        when (content) {
            is String -> {
                contentView.text = content
            }
            is Collection<*> -> {
                content.forEach {
                    contentView.append(it.toString())
                    contentView.append(System.lineSeparator())
                    contentView.append(System.lineSeparator())
                }
                // 删除最后两个空行
                contentView.text = contentView.text.removeSuffix(System.lineSeparator())
                contentView.text = contentView.text.removeSuffix(System.lineSeparator())
            }
            is Map<*, *> -> {
                content.forEach {
                    contentView.append(it.toString())
                    contentView.append(System.lineSeparator())
                    contentView.append(System.lineSeparator())
                    content.entries.forEach { entry ->
                        contentView.append(entry.key.toString() + ":" + entry.value.toString())
                        contentView.append(System.lineSeparator())
                    }
                }
            }
            is View -> {
                contentLayout.addView(content)
            }
        }
        contentLayout.addView(contentView)

        if (onClick != null) {
            card.setOnClickListener(onClick)
        }

        card.setOnLongClickListener {
            val context = App.getContext()
            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textView = it.findViewWithTag<TextView>("content")
            manager.setPrimaryClip(ClipData.newPlainText("LabelForTranslate", textView.text))
            Toast.makeText(App.getContext(), R.string.copied, Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        cardList.add(card)
    }

    /**
     * Play music form url
     * @param url music url
     */
    private fun playMusic(url: String) {
        App.info(tag, JSONObject().apply {
            put("playMusic", url)
        }.toString(4))
        val player = MediaPlayer()
        player.setDataSource(url)
        player.prepareAsync()

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