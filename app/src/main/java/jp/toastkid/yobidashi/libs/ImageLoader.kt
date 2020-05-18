package jp.toastkid.yobidashi.libs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import java.io.IOException

/**
 * Image file loader.

 * @author toastkidjp
 */
object ImageLoader {

    /**
     * Read uri image content.

     * <pre>
     * Uri.invoke(new File(backgroundImagePath).toURI().toString())
    </pre> *

     * @param context Context
     *
     * @param uri Image path uri
     *
     * @return [BitmapDrawable]
     *
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
     *
     * @param uri
     *
     * @return
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor) ?: return null
        parcelFileDescriptor?.close()
        return image
    }

}
