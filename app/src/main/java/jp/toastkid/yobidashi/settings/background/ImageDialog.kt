package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.widget.ImageView

import jp.toastkid.yobidashi.R

/**
 * Dialog wrapper.

 * @author toastkidjp
 */
internal object ImageDialog {

    /**
     * Show dialog with image.
     * @param context
     * *
     * @param uri
     * *
     * @param background
     */
    fun show(context: Context, uri: Uri, background: BitmapDrawable) {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.adjustViewBounds = true
        imageView.setImageDrawable(background)
        AlertDialog.Builder(context)
                .setTitle(R.string.image)
                .setMessage(uri.toString())
                .setView(imageView)
                .setCancelable(true)
                .setPositiveButton(R.string.close) { d, i -> d.dismiss() }
                .show()
    }
}
