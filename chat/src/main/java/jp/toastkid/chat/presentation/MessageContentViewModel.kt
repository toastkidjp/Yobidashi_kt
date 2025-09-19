package jp.toastkid.chat.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.AnnotatedString
import jp.toastkid.lib.image.BitmapScaling
import jp.toastkid.ui.text.KeywordHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class MessageContentViewModel {

    private val keywordHighlighter = KeywordHighlighter()

    private val defaultImage = ImageBitmap(1, 1)

    private val imageHolder = mutableStateOf<ImageBitmap>(defaultImage)

    fun lineText(listLine: Boolean, text: String): AnnotatedString {
        return keywordHighlighter(if (listLine) text.substring(2) else text)
    }

    fun image(): ImageBitmap {
        return imageHolder.value
    }

    suspend fun loadImage(base64Image: String) {
        println("launch image $base64Image")
        val loadImage = withContext(Dispatchers.IO) {
            loadImageInternal(base64Image) ?: defaultImage
        }
        imageHolder.value = loadImage
    }

    private fun loadImageInternal(base64Image: String): ImageBitmap? {
        println("start image")
        val stream = ByteArrayInputStream(
            Base64.decode(base64Image, Base64.NO_WRAP)
        )
        println("stream image")
        val bitmap = stream.use(BitmapFactory::decodeStream)
        println("decoded image")
        if (bitmap == null) {
            return null
        }
        val scaled = BitmapScaling().invoke(bitmap, 360.0, 360.0)
        println("sclaed image")
        return scaled.asImageBitmap()
    }

    fun showImage(base64Image: String?): Boolean {
        if (imageHolder.value == defaultImage) {
            return false
        }
        return base64Image.isNullOrEmpty().not()
    }

}
