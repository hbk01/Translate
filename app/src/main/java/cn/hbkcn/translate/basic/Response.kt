package cn.hbkcn.translate.basic

import org.json.JSONObject

/**
 * Parse the HTTP result(json), quick to get the property
 * @author hbk
 * @since 1.0
 */
class Response(private val json: String) {
    private val jsonObject: JSONObject = JSONObject(json)

    /**
     * Get query
     * @return query content
     */
    fun getQuery(): String {
        return jsonObject.getString("query")
    }

    /**
     * Get translation.
     * @return translation
     */
    fun getTranslation(): ArrayList<String> {
        val list = ArrayList<String>()
        val array = jsonObject.getJSONArray("translation")
        (0 until array.length()).forEach {
            list.add(array.getString(it))
        }
        return list
    }

    /**
     * Get the error code, The code can help to fix problem.
     * @return error code
     */
    fun getErrorCode(): String {
        return jsonObject.optString("errorCode", "0")
    }

    /**
     * Get the from language.
     * @return language
     */
    fun getFromLanguage(): Language {
        val lan = jsonObject.getString("l").split("2")[0]
        return Language.getLanguage(lan)
    }

    /**
     * Get the to language.
     * @return language
     */
    fun getToLanguage(): Language {
        val lan = jsonObject.getString("l").split("2")[1]
        return Language.getLanguage(lan)
    }

    /**
     * Get the from speak url. use the url to play voice.
     * @return url
     */
    fun getFromSpeakUrl(): String = jsonObject.getString("speakUrl")

    /**
     * Get the to speak url. use the url to play voice.
     * @return url
     */
    fun getToSpeakUrl(): String = jsonObject.getString("tSpeakUrl")

    /**
     * Get the web dict.
     * @return web dict, it's maybe a zero list
     */
    fun getWebDict(): ArrayList<String> {
        val list = ArrayList<String>()
        if (jsonObject.has("web")) {
            val array = jsonObject.getJSONArray("web")
            (0 until array.length()).forEach {
                val obj = array.getJSONObject(it)
                val key = obj.getString("key")
                val value = with(StringBuilder()) {
                    val arr = obj.getJSONArray("value")
                    (0 until arr.length()).forEach { index ->
                        append(arr.getString(index))
                        append(", ")
                    }
                    // delete the last 2 chars.
                    delete(length - 2, length)
                    toString()
                }
                list.add("$key: $value")
            }
        }
        return list
    }

    /**
     * Get US phonetic text.
     * @return US phonetic
     */
    fun getUSPhonetic(): String {
        if (jsonObject.has("basic")) {
            val basic = jsonObject.getJSONObject("basic")
            return basic.optString("us-phonetic", "")
        }
        return ""
    }

    /**
     * Get US phonetic speech url.
     * @return url
     */
    fun getUSPhoneticUrl(): String {
        if (jsonObject.has("basic")) {
            val basic = jsonObject.getJSONObject("basic")
            return basic.optString("us-speech", "")
        }
        return ""
    }

    /**
     * Get the UK phonetic text.
     * @return UK phonetic
     */
    fun getUKPhonetic(): String {
        if (jsonObject.has("basic")) {
            val basic = jsonObject.getJSONObject("basic")
            return basic.optString("uk-phonetic", "")
        }
        return ""
    }

    /**
     * Get the UK phonetic speech url.
     * @return url
     */
    fun getUKPhoneticUrl(): String {
        if (jsonObject.has("basic")) {
            val basic = jsonObject.getJSONObject("basic")
            return basic.optString("uk-speech", "")
        }
        return ""
    }

    /**
     * Get the explains.
     * @return explains
     */
    fun getExplains(): ArrayList<String> {
        val list = ArrayList<String>()
        if (jsonObject.has("basic")) {
            val basic = jsonObject.getJSONObject("basic")
            val array = basic.getJSONArray("explains")
            (0 until array.length()).forEach {
                list.add(array.getString(it))
            }
        }
        return list
    }

    override fun toString(): String = json
}
