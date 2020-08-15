package cn.hbkcn.translate.update

import org.json.JSONObject

/**
 * @author hbk
 * @date 6/10/2020
 * @since 1.0
 */
class Response(json: String) {
    private val jsonObject = JSONObject(json)

    /**
     * 错误码，目前只用于标记无网络
     */
    fun errorCode(): String {
        return if (jsonObject.has("errorCode")) {
            jsonObject.getString("errorCode")
        } else {
            ""
        }
    }

    /**
     * 版本名
     * @return version name.
     */
    fun versionName(): String = jsonObject.getString("tag_name")

    /**
     * 获取 version code ，直接从 version name 去掉标点符号转换而来
     * @return 版本号
     */
    fun versionCode(): Int = versionName().removePrefix("v")
        .replace(".", "")
        .toInt()

    /**
     * 更新内容
     * @return update message.
     */
    fun body(): String = jsonObject.getString("body")

    /**
     * apk下载链接
     * @return apk download url.
     */
    fun apkUrl(): String = jsonObject.getJSONArray("assets")
        .getJSONObject(0).getString("browser_download_url")

    /**
     * apk 文件名
     * @return apk file name
     */
    fun apkName(): String = jsonObject.getJSONArray("assets")
        .getJSONObject(0).getString("name")

    /**
     * 检查是否是预览版
     * @return 是为true
     */
    fun preRelease(): Boolean = jsonObject.getBoolean("prerelease")

    /**
     * 更新时间
     * @return YYYY-MM-dd HH:mm:ss
     */
    fun updateTime(): String = jsonObject.getString("created_at")
        .removeSuffix("+08:00")
        .replace("T", " ")

    override fun toString(): String {
        return jsonObject.toString(2)
    }
}