package cn.hbkcn.translate.basic

import cn.hbkcn.translate.App
import okhttp3.*
import okhttp3.Response
import java.io.IOException
import cn.hbkcn.translate.basic.Response as TranslateResponse

class Translate {
    private val baseUrl = "https://openapi.youdao.com/api"
    private val tag = "Translate"

    /**
     * translate. full params.
     * @param query translate the word
     * @param from the query language
     * @param to translate the word to the language
     * @param callback callback
     * @since 1.0
     * @author hbk
     */
    fun translate(
        query: String,
        from: Language = Language.AUTO,
        to: Language = Language.AUTO,
        callback: (TranslateResponse) -> Unit
    ) {
        val body = TranslateBody(query, from, to)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(baseUrl)
            .post(body.toFormBody())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                App.error(tag, "Network Error", e)
                val json = """{"errorCode":"100"}"""
                callback.invoke(TranslateResponse(json))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    App.info(tag, it)
                    callback.invoke(TranslateResponse(it))
                }
            }
        })
    }
}