package cn.hbkcn.translate

import android.app.Application
import android.content.Context

/**
 * @author hbk
 * @date 6/14/2020
 * @since 1.0
 */
class App : Application() {
    override fun onCreate() {
        app = this
        super.onCreate()
    }

    companion object {
        private lateinit var app: App

        /**
         * Get context in anywhere.
         * @return context.
         * @author hbk01
         */
        fun getContext(): Context {
            return app
        }
    }
}