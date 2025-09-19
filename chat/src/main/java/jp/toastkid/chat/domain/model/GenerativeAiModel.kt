package jp.toastkid.chat.domain.model

enum class GenerativeAiModel(
    private val label: String,
    private val urlParameter: String,
    private val versionPath: String,
    private val image: Boolean = false
) {

    GEMINI_2_5_FLASH(
        "Gemini 2.5 Flash",
        "gemini-2.5-flash",
        "v1"
    ),
    GEMINI_2_5_FLASH_LITE(
        "Gemini 2.5 Flash Lite",
        "gemini-2.5-flash-lite",
        "v1beta"
    ),
    GEMINI_2_0_FLASH_IMAGE(
        "Image generation",
        "gemini-2.0-flash-preview-image-generation",
        "v1beta",
        true
    ),
    GEMINI_2_0_FLASH(
        "Gemini 2.0 Flash",
        "gemini-2.0-flash",
        "v1"
    ),
    GEMINI_2_0_FLASH_LITE(
        "Gemini 2.0 Flash Lite",
        "gemini-2.0-flash-lite",
        "v1"
    )
    ;

    fun label(): String = label

    fun url(): String {
        return "https://generativelanguage.googleapis.com/${versionPath}/models/${urlParameter}" +
                ":streamGenerateContent?alt=sse&key="
    }

    fun image() = image

}
