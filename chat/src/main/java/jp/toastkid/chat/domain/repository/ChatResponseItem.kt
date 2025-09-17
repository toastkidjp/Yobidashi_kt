package jp.toastkid.chat.domain.repository

data class ChatResponseItem(
    private val message: String,
    private val error: Boolean = false,
    private val image: Boolean = false
) {

    fun message() = message

    fun error() = error

    fun image() = image

    companion object {

        private val ERROR = ChatResponseItem("[ERROR]", error = true)

        private val OTHER = ChatResponseItem("[OTHER]", error = true)

        fun error() = ERROR

        fun other() = OTHER

    }

}