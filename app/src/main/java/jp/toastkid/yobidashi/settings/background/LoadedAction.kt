package jp.toastkid.yobidashi.settings.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.view.View
import androidx.fragment.app.FragmentActivity
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.BitmapScaling
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
        private val onLoadedAction: () -> Unit
) {

    /** Image file URI.  */
    private val uri: Uri? = data.data

    /**
     * Invoke action.
     */
    operator fun invoke(): Disposable {
        if (uri == null) {
            return Disposables.empty()
        }

        val context = parent.context

        return Maybe.fromCallable {
            val image = ImageLoader.loadBitmap(context, uri)
            image?.let { storeImageToFile(context, it, uri) }
            image
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { bitmap ->
                            onLoadedAction()
                            bitmap?.let { informDone(it) }
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
    private fun storeImageToFile(context: Context, image: Bitmap, uri: Uri) {
        val output = FilesDir(context, BackgroundSettingActivity.BACKGROUND_DIR).assignNewFile(uri)
        PreferenceApplier(context).backgroundImagePath = output.path
        val size = Rect()
        (context as Activity).windowManager.defaultDisplay.getRectSize(size)
        BitmapScaling(image, size.width().toDouble(), size.height().toDouble())
                .compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(output))
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
