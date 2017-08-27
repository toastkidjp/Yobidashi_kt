package jp.toastkid.jitte.libs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView

import java.io.File
import java.io.IOException

/**
 * Image file loader.

 * @author toastkidjp
 */
object ImageLoader {

    /**
     * Read uri image content.

     * <pre>
     * Uri.parse(new File(backgroundImagePath).toURI().toString())
    </pre> *

     * @param context Context
     * *
     * @param uri Image path uri
     * *
     * @return [BitmapDrawable]
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readBitmapDrawable(
            context: Context,
            uri: Uri
    ): BitmapDrawable? {
        val image = loadBitmap(context, uri) ?: return null
        return BitmapDrawable(context.resources, image)
    }

    /**

     * @param context
     * *
     * @param uri
     * *
     * @return
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor) ?: return null
        parcelFileDescriptor.close()
        return image
    }

    /**
     * Set image to passed ImageView.

     * @param iv ImageView
     * *
     * @param imagePath Image file path
     */
    fun setImageToImageView(iv: ImageView, imagePath: String) {
        if (TextUtils.isEmpty(imagePath)) {
            iv.setImageDrawable(null)
            return
        }

        try {
            iv.setImageDrawable(readBitmapDrawable(
                    iv.context,
                    Uri.parse(File(imagePath).toURI().toString())
            ))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
