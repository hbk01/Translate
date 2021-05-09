package cn.hbkcn.translate.update

import android.accounts.NetworkErrorException
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import cn.hbkcn.translate.App
import cn.hbkcn.translate.BuildConfig
import cn.hbkcn.translate.R
import cn.hbkcn.translate.basic.Errors
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import cn.hbkcn.translate.update.Response as UpdateResponse

/**
 * 去执行更新操作
 * @author hbk
 * @date 6/9/2020
 * @since 1.0
 */
class Update(private val activity: Activity) {
    private val tag = "Update"
    private val client = OkHttpClient()
    private val request: Request = with(Request.Builder()) {
        url("https://gitee.com/api/v5/repos/hbk01/Translate/releases/latest")
        addHeader("Content-Type", "application/json")
        addHeader("charset", "UTF-8")
        build()
    }

    private val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var downloadId: Long = -1

    /**
     * Check update and run update work.
     */
    fun update() {
        // Show check update tips.
        val dialog: AlertDialog = AlertDialog.Builder(activity)
            .setMessage(R.string.dialog_checking_update)
            .create()
        dialog.show()

        checkUpdate { response ->
            val error = response.errorCode()
            if (error.isNotEmpty()) {
                activity.runOnUiThread {
                    App.error(tag, "", NetworkErrorException("Not have "))
                    Toast.makeText(activity, Errors(error).toString(), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
                return@checkUpdate
            }

            val code = response.versionCode()
            if (code > BuildConfig.VERSION_CODE) {
                activity.runOnUiThread {
                    App.info(tag, JSONObject().apply {
                        put("errorCode", response.errorCode())
                        put("apkName", response.apkName())
                        put("apkUrl", response.apkUrl())
                        put("versionName", response.versionName())
                        put("versionCode", response.versionCode())
                        put("updateTime", response.updateTime())
                        put("preRelease", response.preRelease())
                        put("body", response.body())
                    }.toString(4))
                    dialog.dismiss()
                    val isPreRelease =
                        if (response.preRelease())
                            activity.getString(R.string.update_true)
                        else
                            activity.getString(R.string.update_false)
                    AlertDialog.Builder(activity)
                        .setMessage(with(StringBuilder()) {
                            append(activity.getString(R.string.update_version_name).format(BuildConfig.VERSION_NAME, response.versionName()))
                            append(System.lineSeparator())
                            append(activity.getString(R.string.update_pre_release).format(isPreRelease))
                            append(System.lineSeparator())
                            append(activity.getString(R.string.update_time).format(response.updateTime()))
                            append(System.lineSeparator())
                            append(activity.getString(R.string.update_change_log))
                            append(System.lineSeparator())
                            append(response.body())
                            toString()
                        })
                        .setPositiveButton(R.string.update) { _, _ ->
                            // 码云要登录才能下载文件（辣鸡），改用 Github 下载地址
                            val url = "https://github.com/hbk01/Translate/releases/download/" +
                                    "${response.versionName()}/${response.apkName()}"
                            download(url, response.apkName())
                        }
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .setNeutralButton(R.string.update_website_download) { _, _ ->
                            // 跳转到浏览器，打开下载页面
                            AlertDialog.Builder(activity)
                                .setMessage(with(java.lang.StringBuilder()) {
                                    append("由于码云下载文件需要登录，所以默认使用的是 github 下载，")
                                    append("而 github 在国内处于半墙状态，下载很不稳定，")
                                    append("所以也提供了码云的下载方式，不过你需要自行登录码云才能开始下载。")
                                    append(System.lineSeparator())
                                    append("点击确定将会打开码云的下载链接，在登录码云后会自动开始下载更新包。")
                                    append(System.lineSeparator())
                                    append(System.lineSeparator())
                                    append("最后说一句，码云真好。:)")
                                    toString()
                                })
                                .setPositiveButton(R.string.dialog_ok) { _, _ ->
                                    val uri = Uri.parse(response.apkUrl())
                                    App.getContext().startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show()
                        }
                        .create()
                        .show()
                }
            } else {
                activity.runOnUiThread {
                    Toast.makeText(activity, R.string.update_no_update, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun checkUpdate(response: (UpdateResponse) -> Unit) {
        connect {
            response.invoke(UpdateResponse(it))
        }
    }

    private fun download(url: String, apkName: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(apkName)
        request.setDescription(url)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName)

        val id = manager.enqueue(request)
        downloadId = id
    }

    @Deprecated("No action need install apk.")
    private fun install() {
        if (this.downloadId != -1L) {
            val uri = manager.getUriForDownloadedFile(downloadId)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            activity.startActivity(intent)
        }
    }

    @Deprecated("本来应该由下载广播接收器来调用的，懒得调用安装了，让用户自己安装")
    private fun checkStatus() {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)

        val cursor = manager.query(query)
        if (cursor.moveToFirst()) {
            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_FAILED -> {
                    Toast.makeText(activity, "Download Failed", Toast.LENGTH_SHORT).show()
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    Toast.makeText(activity, "Download Success", Toast.LENGTH_SHORT).show()
                    // install()
                }
                else -> Toast.makeText(activity, "Other", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connect(callback: (String) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            val errorCode: String = """
                {
                    "errorCode": "100"
                }
            """.trimIndent()

            override fun onFailure(call: Call, e: IOException) {
                callback.invoke(errorCode)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json.isNullOrEmpty()) {
                    callback.invoke(errorCode)
                } else {
                    callback.invoke(json)
                }
            }
        })
    }

}