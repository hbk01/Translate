package cn.hbkcn.translate

import org.junit.Test

class TranslateUnitTest {

    @Test
    fun testTranslate() {
        val translate = Translate()
        val auto = Translate.Language.自动识别
        translate.translate("hello", auto, auto, object : ResponseCallback {
            override fun callback(json: String) {
                println(json)
            }
        })
        Thread.sleep(5000)
    }
}