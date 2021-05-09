package cn.hbkcn.translate.basic

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import cn.hbkcn.translate.App
import cn.hbkcn.translate.R
import okhttp3.FormBody
import org.json.JSONObject
import java.security.MessageDigest

class TranslateBody constructor(query: String, from: Language, to: Language) {
    private val context: Context = App.getContext()
    private val preference: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val body: HashMap<String, String> = HashMap()
    private val tag = "TranslateBody"

    private val appId: String = with(preference) {
        val id = getString(context.getString(R.string.preference_key_appid), "")
        if (id.isNullOrEmpty()) {
            "3cd47726b488d5ad"
        } else {
            id
        }
    }

    private val appKey: String = with(preference) {
        val key = getString(context.getString(R.string.preference_key_appkey), "")
        if (key.isNullOrEmpty()) {
            "WiAQEJoYTzUcF9We6HnwkFAy7NByYfMt"
        } else {
            key
        }
    }

    init {
        val time = System.currentTimeMillis()
        val salt = time.toString()
        val curtime = (time / 1000).toString()
        App.info(tag, JSONObject().apply {
            put("appId", appId)
            put("appKey", appKey)
        }.toString(4))
        body["q"] = query
        body["from"] = from.code
        body["to"] = to.code
        body["appKey"] = appId
        body["salt"] = salt
        body["curtime"] = curtime
        body["sign"] = sign(query, salt, curtime)
        body["signType"] = "v3"
        body["ext"] = "mp3"
        body["voice"] = "0"
        body["strict"] = "true"
    }

    fun toFormBody(): FormBody {
        val formBody = FormBody.Builder()
        body.onEach { entry ->
            formBody.addEncoded(entry.key, entry.value)
        }
        return formBody.build()
    }

    private fun sign(query: String, salt: String, curtime: String): String {
        val input: String = with(StringBuilder()) {
            if (query.length > 20) {
                append(query.substring(0..9))
                append(query.length)
                append(query.substring(query.length - 10, query.length))
            } else if (query.length <= 20) {
                append(query)
            }
            toString()
        }
        return sha256(appId + input + salt + curtime + appKey)
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("sha-256")
        val byteArray = md.digest(input.toByteArray())
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }
}

