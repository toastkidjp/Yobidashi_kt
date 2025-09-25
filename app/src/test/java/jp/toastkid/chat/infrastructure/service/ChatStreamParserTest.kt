package jp.toastkid.chat.infrastructure.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChatStreamParserTest {

    private lateinit var subject: ChatStreamParser

    private val line = "data: {\"candidates\": [{\"content\": {\"parts\": " +
            "[{\"text\": \"**材料 (1人分)**\\n\\n**スープ**\\n* 豚骨または\"}],\"role\": \"model\"},\"finishReason\": " +
            "\"STOP\",\"index\": 0,\"safetyRatings\": [{\"category\": \"HARM_CATEGORY_SEXUALLY_EXPLICIT\"," +
            "\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HATE_SPEECH\"," +
            "\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HARASSMENT\",\"probability\":" +
            " \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_DANGEROUS_CONTENT\",\"probability\": \"NEGLIGIBLE\"}]}]," +
            "\"promptFeedback\": {\"safetyRatings\": [{\"category\": \"HARM_CATEGORY_SEXUALLY_EXPLICIT\"," +
            "\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HATE_SPEECH\"," +
            "\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HARASSMENT\"," +
            "\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_DANGEROUS_CONTENT\"," +
            "\"probability\": \"NEGLIGIBLE\"}]}}\n"

    @Before
    fun setUp() {
        subject = ChatStreamParser()
    }

    @Test
    fun invoke() {
        val item = subject.invoke(line) ?: return fail()

        assertEquals("""**材料 (1人分)**

**スープ**
* 豚骨または""".trimIndent(), item.message()
        )
        assertFalse(item.error())
        assertFalse(item.image())
    }

    @Test
    fun unacceptableFormatCase() {
        assertNull(subject.invoke("test"))
    }

    @Test
    fun error() {
        val item =
            subject.invoke("data: {\"candidates\": [{\"finishReason\": \"SAFETY\",\"index\": 0,\"safetyRatings\": [{\"category\": \"HARM_CATEGORY_SEXUALLY_EXPLICIT\",\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HATE_SPEECH\",\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HARASSMENT\",\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_DANGEROUS_CONTENT\",\"probability\": \"LOW\"}]}],\"usageMetadata\": {\"promptTokenCount\": 73,\"totalTokenCount\": 73}}\n") ?: return fail()

        assertEquals("[ERROR]", item.message())
        assertTrue(item.error())
        assertFalse(item.image())
    }

    @Test
    fun otherError() {
        val item =
            subject.invoke("data: {\"candidates\": [{\"finishReason\": \"OTHER\",\"index\": 0,\"safetyRatings\": [{\"category\": \"HARM_CATEGORY_SEXUALLY_EXPLICIT\",\"probability\": \"NEGLIGIBLE\"},{\"category\": \"HARM_CATEGORY_HATE_SPEECH\",\"probability\": \"LOW\"},{\"category\": \"HARM_CATEGORY_HARASSMENT\",\"probability\": \"LOW\"},{\"category\": \"HARM_CATEGORY_DANGEROUS_CONTENT\",\"probability\": \"NEGLIGIBLE\"}]}],\"usageMetadata\": {\"promptTokenCount\": 1996,\"totalTokenCount\": 1996}}\n") ?: return fail()

        assertEquals("[OTHER]", item.message())
        assertTrue(item.error())
        assertFalse(item.image())
    }

    @Test
    fun image() {
        val item =
            subject.invoke("data: {\"candidates\": [{\"content\": {\"parts\": [{\"inlineData\": {\"mimeType\": \"image/png\",\"data\": \"base64encoded-image\" } }],\"role\": \"model\"},\"index\": 0}],\"usageMetadata\": {\"promptTokenCount\": 8,\"candidatesTokenCount\": 1341,\"totalTokenCount\": 1349,\"promptTokensDetails\": [{\"modality\": \"TEXT\",\"tokenCount\": 8}],\"candidatesTokensDetails\": [{\"modality\": \"IMAGE\",\"tokenCount\": 1290}]}}") ?: return fail()

        assertEquals("base64encoded-image", item.message())
        assertFalse(item.error())
        assertTrue(item.image())
    }

    @Test
    fun irregularCase() {
        assertNull(subject.invoke("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"\"}],\"role\": \"model\"},\"finishReason\": \"STOP\"}],\"usageMetadata\": {\"promptTokenCount\": 712,\"candidatesTokenCount\": 19,\"totalTokenCount\": 731,\"promptTokensDetails\": [{\"modality\": \"TEXT\",\"tokenCount\": 712}],\"candidatesTokensDetails\": [{\"modality\": \"TEXT\",\"tokenCount\": 19}]},\"modelVersion\": \"gemini-2.0-flash\",\"responseId\": \"gVc8aKuXPNHB7dcPwf7a4QY\"}\n"))
    }

}
