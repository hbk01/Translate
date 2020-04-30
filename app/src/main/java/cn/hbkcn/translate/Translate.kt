package cn.hbkcn.translate

import okhttp3.*
import okhttp3.Response
import java.io.IOException

class Translate {
    private val baseUrl = "https://openapi.youdao.com/api"

    enum class Language constructor(val code: String) {
        自动识别("auto"),
        中文("zh-CHS"),
        英文("en"),
        日文("ja"),
        韩文("ko"),
        法文("fr"),
        俄文("ru"),
        越南文("vi"),
        德文("de"),
        印尼文("id"),
        粤语("yue"),
        马来语("ms"),
        泰语("th"),
        冰岛语("is"),
        蒙古语("mn"),
        缅甸语("my");
    }

    fun translate(query: String, from: Language, to: Language, callback: ResponseCallback) {
        val body = TranslateBody(query, from, to)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(baseUrl)
            .post(body.toFormBody())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("onFailure")
                callback.callback(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                println("onResponse")
                // todo 可以获取到json
                response.body?.string()?.let { callback.callback(it) }
            }
        })
    }

}