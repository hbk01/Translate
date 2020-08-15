package cn.hbkcn.translate.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
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
class Update(private val context: Context) {
    private val client = OkHttpClient()
    private val request: Request = with(Request.Builder()) {
        url("https://gitee.com/api/v5/repos/hbk01/Translate/releases/latest")
        addHeader("Content-Type", "application/json")
        addHeader("charset", "UTF-8")
        build()
    }

    private val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var downloadId: Long = -1

    fun checkUpdate(response: (UpdateResponse) -> Unit) {
        connect {
            response.invoke(UpdateResponse(it))
        }
    }

    fun download(url: String, apkName: String) {
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
            context.startActivity(intent)
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
                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show()
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    Toast.makeText(context, "Download Success", Toast.LENGTH_SHORT).show()
                    // install()
                }
                else -> Toast.makeText(context, "Other", Toast.LENGTH_SHORT).show()
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