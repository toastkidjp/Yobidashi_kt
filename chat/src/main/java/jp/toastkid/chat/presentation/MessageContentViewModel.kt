package jp.toastkid.chat.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.createBitmap
import jp.toastkid.ui.text.KeywordHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream

class MessageContentViewModel {

    private val keywordHighlighter = KeywordHighlighter()

    private val defaultImage = createBitmap(1, 1)

    private val imageHolder = mutableStateOf(defaultImage)

    fun lineText(listLine: Boolean, text: String): AnnotatedString {
        return keywordHighlighter(if (listLine) text.substring(2) else text)
    }

    fun image(): Bitmap {
        return imageHolder.value
    }

    suspend fun loadImage(base64Image: String) {
        val loadImage = withContext(Dispatchers.IO) {
            loadImageInternal(base64Image) ?: defaultImage
        }
        imageHolder.value = loadImage
    }

    private fun loadImageInternal(base64Image: String): Bitmap? {
        val stream = BufferedInputStream(ByteArrayInputStream(Base64.decode(base64Image, Base64.NO_WRAP)))
        val bitmap = stream.use(BitmapFactory::decodeStream)
        if (bitmap == null) {
            return null
        }
        return bitmap
    }

    fun showImage(base64Image: String?): Boolean {
        if (imageHolder.value == defaultImage) {
            return false
        }
        return base64Image.isNullOrEmpty().not()
    }

    private val openImageDropdownMenu = mutableStateOf(false)

    fun openingImageDropdownMenu() = openImageDropdownMenu.value

    fun openImageDropdownMenu() {
        openImageDropdownMenu.value = true
    }

    fun closeImageDropdownMenu() {
        openImageDropdownMenu.value = false
    }

}
