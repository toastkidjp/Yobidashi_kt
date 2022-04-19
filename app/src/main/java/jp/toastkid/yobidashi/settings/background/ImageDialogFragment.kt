package jp.toastkid.yobidashi.settings.background

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import coil.compose.AsyncImage
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.yobidashi.R
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

        val arguments = arguments ?: return super.onCreateDialog(savedInstanceState)

        val contentView = ComposeViewFactory().invoke(activityContext) {
            var scale by remember { mutableStateOf(1f) }
            var rotation by remember { mutableStateOf(0f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                scale *= zoomChange
                rotation += rotationChange
                offset += offsetChange
            }

            val model: Any? = when {
                arguments.containsKey(KEY_IMAGE) -> arguments.getParcelable<Bitmap>(KEY_IMAGE)
                arguments.containsKey(KEY_IMAGE_URL) -> arguments.getString(KEY_IMAGE_URL)
                else -> R.drawable.ic_image
            }
            AsyncImage(
                model = model,
                contentDescription = stringResource(id = R.string.image),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotation,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = state)
                    .padding(end = 16.dp)
            )
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.image)
                .setView(contentView)
                .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
                .create()
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
