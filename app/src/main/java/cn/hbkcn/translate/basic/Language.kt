package cn.hbkcn.translate.basic

enum class Language(val code: String) {
    /**
     * 自动识别
     */
    AUTO("auto"),

    /**
     * 中文
     */
    CHINESE("zh-CHS"),

    /**
     * 英文
     */
    ENGLISH("en"),

    /**
     * 日文
     */
    JAPANESE("ja"),

    /**
     * 韩文
     */
    KOREAN("ko"),

    /**
     * 法文
     */
    FRENCH("fr"),

    /**
     * 俄文
     */
    RUSSIAN("ru"),

    /**
     * 德文
     */
    GERMAN("de");

    companion object {
        /**
         * get Language object use code
         * @param code The language code
         */
        fun getLanguage(code: String): Language {
            return with(code) {
                // I know this is stupid...
                when {
                    equals("zh-CHS") -> CHINESE
                    equals("en") -> ENGLISH
                    equals("ja") -> JAPANESE
                    equals("ko") -> KOREAN
                    equals("fr") -> FRENCH
                    equals("ru") -> RUSSIAN
                    equals("de") -> GERMAN
                    else -> AUTO
                }
            }
        }
    }
}