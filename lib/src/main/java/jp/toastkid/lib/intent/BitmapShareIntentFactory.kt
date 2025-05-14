package jp.toastkid.lib.intent

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import jp.toastkid.lib.image.ImageCache

class BitmapShareIntentFactory(
    private val imageCache: ImageCache = ImageCache()
) {

    operator fun invoke(context: Context, bitmap: Bitmap): Intent {
        val intent = Intent(Intent.ACTION_ATTACH_DATA)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageCache.saveBitmap(context.cacheDir, bitmap).absoluteFile
        )
        intent.setDataAndType(uri, MIME_TYPE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return intent
    }

    companion object {
        private const val MIME_TYPE = "image/*"
    }

}
