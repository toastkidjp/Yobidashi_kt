package jp.toastkid.chat.domain.model

enum class GenerativeAiModel(
    private val label: String,
    private val urlParameter: String,
    private val versionPath: String,
    private val webGrounding: Boolean = false,
    private val image: Boolean = false
) {

    GEMINI_3_0_FLASH(
        "Gemini 3 Flash",
        "gemini-3-flash-preview",
        "v1beta"
    ),
    GEMINI_2_5_FLASH(
        "Gemini 2.5 Flash",
        "gemini-2.5-flash",
        "v1beta",
        webGrounding = true,
    ),
    GEMINI_2_5_FLASH_LITE(
        "Gemini 2.5 Flash Lite",
        "gemini-2.5-flash-lite",
        "v1beta",
        webGrounding = true,
    ),
    GEMINI_2_5_FLASH_WITHOUT_WEB_GROUNDING(
        "Gemini 2.5 Flash(Web Grounding なし)",
        "gemini-2.5-flash",
        "v1beta",
    ),
    GEMINI_2_5_FLASH_LITE_WITHOUT_WEB_GROUNDING(
        "Gemini 2.5 Flash Lite(Web Grounding なし)",
        "gemini-2.5-flash-lite",
        "v1beta",
    ),
    ;

    fun label(): String = label

    fun url(): String {
        return "https://generativelanguage.googleapis.com/${versionPath}/models/${urlParameter}" +
                ":streamGenerateContent?alt=sse&key="
    }

    fun webGrounding() = webGrounding

    fun image() = image

    fun version() = urlParameter.split("-")[1]

}
