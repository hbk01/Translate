package cn.hbkcn.translate.basic

import okhttp3.FormBody
import java.security.MessageDigest

class TranslateBody constructor(query: String, from: Language, to: Language) {
    private val body: HashMap<String, String> = HashMap()
    private val appId: String = "3cd47726b488d5ad"
    private val appKey: String = "WiAQEJoYTzUcF9We6HnwkFAy7NByYfMt"

    init {
        val time = System.currentTimeMillis()
        val salt = time.toString()
        val curtime = (time / 1000).toString()
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
        val input: String = with(java.lang.StringBuilder()) {
            if (query.length > 20) {
                append(query.substring(0..10))
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