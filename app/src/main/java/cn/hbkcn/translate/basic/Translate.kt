package cn.hbkcn.translate.basic

import android.util.Log
import okhttp3.*
import okhttp3.Response
import java.io.IOException
import cn.hbkcn.translate.basic.Response as TranslateResponse

class Translate {
    private val baseUrl = "https://openapi.youdao.com/api"

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
                // val json = """{"errorCode":"-1", "errorMsg":"${e.message}"}"""
                Log.e("HTTP", "request error", e)
            }

            override fun onResponse(call: Call, response: Response) {
                println("onResponse")
                response.body?.string()?.let {
                    println(it)
                    callback.invoke(TranslateResponse(it))
                }
            }
        })
    }
}