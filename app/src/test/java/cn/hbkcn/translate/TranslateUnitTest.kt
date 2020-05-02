package cn.hbkcn.translate

import cn.hbkcn.translate.basic.Response
import cn.hbkcn.translate.basic.Translate
import org.junit.Test

class TranslateUnitTest {

    @Test
    fun testTranslate() {
        val translate = Translate()
        translate.translate("maybe") { response: Response ->
            if (response.getErrorCode() == "0") {
                println("${response.getFromLanguage()} -> ${response.getToLanguage()}")
                println("from speak url: ${response.getFromSpeakUrl()}")
                println("  to speak url: ${response.getToSpeakUrl()}")
                println("   translation: ${response.getTranslation()}")
                println("      web dict: ")
                response.getWebDict().forEach {
                    println("           $it")
                }
            }
        }
        // wait response
        Thread.sleep(5000)
    }
}