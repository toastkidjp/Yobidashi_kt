package jp.toastkid.chat.infrastructure.repository

import jp.toastkid.chat.domain.repository.ChatRepository
import jp.toastkid.chat.infrastructure.service.ChatStreamParser
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class ChatApi(
    private val apiKey: String,
    private val apiUrl: String,
) : ChatRepository {

    override fun request(content: String, streamLineConsumer: (String?) -> Unit) {
        val connection = URL("$apiUrl$apiKey").openConnection() as? HttpURLConnection ?: return
        connection.setRequestProperty("Content-Type", "application/json")
        connection.requestMethod = "POST"
        connection.doInput = true
        connection.doOutput = true
        connection.readTimeout = 60_000
        connection.connect()

        BufferedWriter(OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8)).use {
            it.write(content)
        }

        if (connection.responseCode != 200) {
            return
        }

        val parser = ChatStreamParser()
        BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use {
            var line = it.readLine()
            while (line != null) {
                val response = parser.invoke(line)
                if (response != null) {
                    streamLineConsumer(response)
                }

                line = it.readLine()
            }
        }
    }

}
