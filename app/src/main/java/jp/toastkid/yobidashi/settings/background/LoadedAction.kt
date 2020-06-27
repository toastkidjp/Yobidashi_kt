package jp.toastkid.yobidashi.settings.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.view.View
import androidx.fragment.app.FragmentActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.BitmapScaling
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Action of loaded new background image.
 *
 * @param data
 * @param parent Snackbar's parent.
 * @param colorPair Color pair.
 * @param onLoadedAction On loaded action.
 *
 * @author toastkidjp
 */
internal class LoadedAction (
        data: Intent,
        private val parent: View,
        private val colorPair: ColorPair,
        private val onLoadedAction: () -> Unit,
        private val fileDir: String
) {

    /**
     * Image file URI.
     */
    private val uri: Uri? = data.data

    /**
     * For fixing rotated image.
     */
    private val rotatedImageFixing = RotatedImageFixing()

    /**
     * Invoke action.
     */
    operator fun invoke() {
        if (uri == null) {
            return
        }

        val context = parent.context

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = try {
                withContext(Dispatchers.IO) {
                    val image = ImageLoader.loadBitmap(context, uri)
                    val fixedImage = rotatedImageFixing(context.contentResolver, image, uri)
                    fixedImage?.let { storeImageToFile(context, it, uri) }
                    fixedImage
                }
            } catch (e: IOException) {
                Timber.e(e)
                informFailed()
                return@launch
            }

            onLoadedAction()
            bitmap?.let { informDone(it) }
        }
    }

    /**
     * Store image file.
     *
     * @param context
     * @param image
     *
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun storeImageToFile(context: Context, image: Bitmap, uri: Uri) {
        val output = FilesDir(context, fileDir).assignNewFile(uri)
        PreferenceApplier(context).backgroundImagePath = output.path
        val size = Rect()
        (context as? Activity)?.windowManager?.defaultDisplay?.getRectSize(size)
        val fileOutputStream = FileOutputStream(output)
        BitmapScaling(image, size.width().toDouble(), size.height().toDouble())
                .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
    }

    /**
     * Inform failed.
     */
    private fun informFailed() {
        Toaster.snackShort(parent, R.string.message_failed_read_image, colorPair)
    }

    /**
     * Inform done with action.
     *
     * @param image
     */
    private fun informDone(image: Bitmap) {
        Toaster.snackLong(
                parent,
                R.string.message_done_set_image,
                R.string.display,
                View.OnClickListener{ v ->
                    val viewContext = v.context
                    if (viewContext is FragmentActivity) {
                        ImageDialogFragment.withBitmap(image)
                                .show(
                                        viewContext.supportFragmentManager,
                                        ImageDialogFragment::class.java.simpleName
                                )
                    }
                },
                colorPair
        )
    }

}
