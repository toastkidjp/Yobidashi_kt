package jp.toastkid.yobidashi.settings.background.load

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.image.ImageStoreUseCase
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.lib.window.WindowRectCalculatorCompat
import jp.toastkid.yobidashi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

/**
 * Action of loaded new background image.
 *
 * @param uri Image file URI.
 * @param parent Snackbar's parent.
 * @param colorPair Color pair.
 * @param onLoadedAction On loaded action.
 *
 * @author toastkidjp
 */
internal class LoadedAction (
        private val uri: Uri?,
        private val context: Context,
        private val contentViewModel: ContentViewModel,
        private val onLoadedAction: () -> Unit,
        private val fileDir: String
) {

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

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = try {
                withContext(Dispatchers.IO) {
                    val image = context.imageLoader
                            .execute(ImageRequest.Builder(context).data(uri).build())
                            .drawable
                            ?.toBitmap()

                    val fixedImage = rotatedImageFixing(context.contentResolver, image, uri)
                    fixedImage?.let {
                        val displaySize = WindowRectCalculatorCompat().invoke(context as? Activity) ?: return@let
                        ImageStoreUseCase(
                                FilesDir(context, fileDir),
                                PreferenceApplier(context)
                        )(it, uri, displaySize)
                    }
                    fixedImage
                }
            } catch (e: IOException) {
                Timber.e(e)
                informFailed()
                return@launch
            }

            onLoadedAction()
            bitmap?.let { contentViewModel.snackShort(R.string.message_done_set_image) }
        }
    }

    /**
     * Inform failed.
     */
    private fun informFailed() {
        contentViewModel.snackShort(R.string.message_failed_read_image)
    }

}
