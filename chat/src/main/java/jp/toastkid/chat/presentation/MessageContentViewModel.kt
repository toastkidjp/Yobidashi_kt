package jp.toastkid.chat.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.AnnotatedString
import jp.toastkid.ui.text.KeywordHighlighter
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicReference

class MessageContentViewModel {

    private val keywordHighlighter = KeywordHighlighter()

    private val imageHolder = AtomicReference<ImageBitmap>()

    fun lineText(listLine: Boolean, text: String): AnnotatedString {
        return keywordHighlighter(if (listLine) text.substring(2) else text)
    }

    fun image(base64Image: String): ImageBitmap {
        val current = imageHolder.get()
        if (current != null) {
            return current
        }

        val loadImage = loadImage(base64Image)
        imageHolder.set(loadImage)

        return imageHolder.get()
    }

    private fun loadImage(base64Image: String): ImageBitmap {
        val stream = ByteArrayInputStream(
            Base64.decode(base64Image, Base64.NO_WRAP)
        )
        return stream.use { BitmapFactory.decodeStream(stream) }.asImageBitmap()
    }

}
