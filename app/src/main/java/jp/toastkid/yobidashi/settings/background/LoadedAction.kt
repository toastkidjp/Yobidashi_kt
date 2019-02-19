package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.FragmentActivity
import android.view.View
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * Action of loaded new background image.
 *
 * @param data
 * @param parent
 * @param colorPair
 * @param onLoadedAction
 *
 * @author toastkidjp
 */
internal class LoadedAction (
        data: Intent,
        /** Snackbar's parent.  */
        private val parent: View,
        /** Color pair.  */
        private val colorPair: ColorPair,
        /** On loaded action.  */
        private val onLoadedAction: () -> Unit
) {

    /** Image file URI.  */
    private val uri: Uri = data.data

    /**
     * Invoke action.
     */
    operator fun invoke() {
        val context = parent.context

        Maybe.fromCallable {
            val image = ImageLoader.loadBitmap(context, uri)
            image?.let { storeImageToFile(context, it) }
            image
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            onLoadedAction()
                            it?.let { informDone(it) }
                        },
                        {
                            Timber.e(it)
                            informFailed()
                        }
                )
    }

    /**
     * Store image file.
     * @param context
     *
     * @param image
     *
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun storeImageToFile(context: Context, image: Bitmap) {
        val output = FilesDir(context, BackgroundSettingActivity.BACKGROUND_DIR).assignNewFile(uri)
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
