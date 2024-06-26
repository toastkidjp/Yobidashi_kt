package jp.toastkid.chat.domain.repository

import jp.toastkid.chat.domain.model.Chat

interface ChatExporter {

    operator fun invoke(chat: Chat)

}