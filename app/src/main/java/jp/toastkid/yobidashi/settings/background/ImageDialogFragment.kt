package jp.toastkid.yobidashi.settings.background

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
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

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext: Context = context
                ?: return super.onCreateDialog(savedInstanceState)

        val contentView = LayoutInflater.from(activityContext)
                .inflate(R.layout.content_dialog_image, null)

        val arguments = arguments ?: return super.onCreateDialog(savedInstanceState)

        loadImage(contentView, arguments)

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.image)
                .setView(contentView)
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
                .create()
    }

    private fun loadImage(contentView: View, arguments: Bundle) {
        val imageView = contentView.findViewById<ImageView>(R.id.image)
        when {
            arguments.containsKey(KEY_IMAGE) -> {
                val bitmap = arguments.getParcelable<Bitmap>(KEY_IMAGE)
                Glide.with(imageView)
                        .load(bitmap)
                        .into(imageView)
            }
            arguments.containsKey(KEY_IMAGE_URL) -> {
                val uriString = arguments.getString(KEY_IMAGE_URL)
                Glide.with(imageView)
                        .load(uriString)
                        .into(imageView)
            }
        }
    }

    companion object {

        private const val KEY_IMAGE = "image"

        private const val KEY_IMAGE_URL = "imageUrl"

        fun withUrl(imageUrl: String): ImageDialogFragment =
                ImageDialogFragment().also {
                    it.arguments = bundleOf(KEY_IMAGE_URL to imageUrl)
                }

        fun withBitmap(image: Bitmap): ImageDialogFragment =
                ImageDialogFragment().also {
                    it.arguments = bundleOf(KEY_IMAGE to image)
                }
    }
}
