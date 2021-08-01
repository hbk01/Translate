package cn.hbkcn.translate

import java.io.PrintWriter
import java.io.StringWriter

class ExceptionHandler: Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    fun install() {
        // 将当前类设为默认异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (handleException(e)) {
            defaultHandler?.uncaughtException(t, e)
        }
    }

    private fun handleException(e: Throwable): Boolean {
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        App.error(e.javaClass.simpleName, "${e.message}", e)
        return true
    }
}