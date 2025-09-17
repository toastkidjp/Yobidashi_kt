package jp.toastkid.chat.domain.repository

interface ChatRepository {

    fun request(content: String, streamLineConsumer: (ChatResponseItem?) -> Unit)

}