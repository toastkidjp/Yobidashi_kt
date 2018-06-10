package jp.toastkid.yobidashi.settings.background

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.ImageView
import androidx.core.net.toUri
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.settings.background.ImageDialogFragment.Companion.withBitmap

/**
 * Image dialog fragment.
 * You should make this instance with [withBitmap] function.
 *
 * @author toastkidjp
 */
internal class ImageDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext: Context = context
                ?: return super.onCreateDialog(savedInstanceState)

        val drawable: Drawable = when {
            arguments?.containsKey(KEY_IMAGE) ?: false -> {
                BitmapDrawable(activityContext.resources, arguments?.getParcelable<Bitmap>(KEY_IMAGE))
            }
            arguments?.containsKey(KEY_IMAGE_URL) ?: false -> {
                val uriString = arguments?.getString(KEY_IMAGE_URL)
                val uri = uriString?.toUri()
                        ?: return super.onCreateDialog(savedInstanceState)
                ImageLoader.readBitmapDrawable(activityContext, uri)
            }
            else -> null
        } ?: return super.onCreateDialog(savedInstanceState)

        val imageView = ImageView(activityContext)
                .also {
                    it.scaleType = ImageView.ScaleType.CENTER_CROP
                    it.adjustViewBounds = true
                    it.setImageDrawable(drawable)
                }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.image)
                .setView(imageView)
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
                .create()
    }

    companion object {

        private const val KEY_IMAGE = "image"

        private const val KEY_IMAGE_URL = "imageUrl"

        fun withUrl(imageUrl: String): ImageDialogFragment =
                ImageDialogFragment()
                        .also { it.arguments?.putString(KEY_IMAGE_URL, imageUrl) }

        fun withBitmap(image: Bitmap): ImageDialogFragment =
                ImageDialogFragment()
                        .also { it.arguments?.putParcelable(KEY_IMAGE, image) }
    }
}
