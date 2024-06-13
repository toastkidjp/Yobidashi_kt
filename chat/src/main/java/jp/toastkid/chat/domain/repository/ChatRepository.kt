package jp.toastkid.chat.domain.repository

interface ChatRepository {

    fun request(content: String, streamLineConsumer: (String?) -> Unit)

}