package jp.toastkid.jitte.settings.background

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.View
import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.ImageLoader
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.preference.ColorPair
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import jp.toastkid.jitte.libs.storage.Storeroom
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Action of loaded new background image.

 * @author toastkidjp
 */
internal class LoadedAction
/**

 * @param data
 * *
 * @param parent
 * *
 * @param colorPair
 * *
 * @param onLoadedAction
 */
(
        data: Intent,
        /** Snackbar's parent.  */
        private val parent: View,
        /** Color pair.  */
        private val colorPair: ColorPair,
        /** On loaded action.  */
        private val onLoadedAction: () -> Unit
) {

    /** Image file URI.  */
    private val uri: Uri

    init {
        this.uri = data.data
    }

    /**
     * Invoke action.
     */
    operator fun invoke() {

        try {
            val context = parent.context

            val image = ImageLoader.loadBitmap(context, uri)

            if (image == null) {
                informFailed()
                return
            }

            storeImageToFile(context, image)

            onLoadedAction()

            informDone(context, image)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * Store image file.
     * @param context
     * *
     * @param image
     * *
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun storeImageToFile(context: Context, image: Bitmap) {
        val output = Storeroom(context, BackgroundSettingActivity.BACKGROUND_DIR).assignNewFile(uri)
        PreferenceApplier(context).backgroundImagePath = output.path
        image.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(output))
    }

    /**
     * Inform failed.
     */
    private fun informFailed() {
        Toaster.snackShort(parent, R.string.message_failed_read_image, colorPair)
    }

    /**
     * Inform done with action.
     * @param context
     * *
     * @param image
     */
    private fun informDone(context: Context, image: Bitmap) {
        Toaster.snackLong(
                parent, R.string.message_done_set_image, R.string.display,
                View.OnClickListener{ v -> ImageDialog.show(context, uri, BitmapDrawable(context.resources, image)) },
                colorPair
        )
    }

}
