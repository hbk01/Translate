package cn.hbkcn.translate.update

import android.app.Activity
import android.util.Log.e
import cn.hbkcn.translate.BuildConfig
import okhttp3.*
import okhttp3.Response
import java.io.IOException
import cn.hbkcn.translate.update.Response as UpdateResponse

/**
 * 去执行更新操作
 * @author hbk
 * @date 6/9/2020
 * @since 1.0
 */
class Update(private val context: Activity) {
    private val token = "1632b9fc9ced694e3bc5c942e75f5960"
    private val client = OkHttpClient()
    private val request: Request = with(Request.Builder()) {
        url("https://gitee.com/api/v5/repos/hbk01/Translate/releases/latest?access_token=$token")
        addHeader("Content-Type", "application/json")
        addHeader("charset", "UTF-8")
        build()
    }

    fun checkUpdate(ifHasUpdate: (UpdateResponse) -> Unit) {
        e("checkUpdate", "start connect.")
        connect {
            e("result", it)
            if (it != "error") {
                val response = UpdateResponse(it)
                val code = response.versionCode()
                e("code", "$code:${BuildConfig.VERSION_CODE}")
                if (code > BuildConfig.VERSION_CODE) {
                    ifHasUpdate.invoke(response)
                }
            }
        }
    }

    private fun connect(callback: (String) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.invoke("error")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json.isNullOrEmpty()) {
                    callback.invoke("error")
                } else {
                    callback.invoke(json)
                }
            }
        })
    }
}