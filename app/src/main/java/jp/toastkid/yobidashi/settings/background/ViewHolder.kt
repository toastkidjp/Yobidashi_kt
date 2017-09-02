package jp.toastkid.yobidashi.settings.background

import android.net.Uri
import android.support.v7.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.SavedImageBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Extended of [RecyclerView.ViewHolder].
 *
 * @param binding
 * @param preferenceApplier
 * @param onRemoved
 *
 * @author toastkidjp
 */
internal class ViewHolder(
        /** Binding object.  */
        private val binding: SavedImageBinding,
        /** Preferences wrapper.  */
        private val preferenceApplier: PreferenceApplier,
        /** Action on removed.  */
        private val onRemoved: Runnable
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Apply file content.
     * @param f
     */
    fun applyContent(f: File) {
        ImageLoader.setImageToImageView(this.binding.image, f.path)
        this.binding.text.text = f.name
        this.binding.remove.setOnClickListener ({ v -> removeSetImage(f) })
        this.binding.root.setOnClickListener ({ v ->
            preferenceApplier.backgroundImagePath = f.path
            Toaster.snackShort(
                    binding.image,
                    R.string.message_change_background_image,
                    preferenceApplier.colorPair()
            )
        })
        this.binding.root.setOnLongClickListener ({ v ->
            val uri = Uri.parse(f.toURI().toString())
            try {
                ImageDialog.show(
                        v.context, uri, ImageLoader.readBitmapDrawable(v.context, uri)!!)
            } catch (e: IOException) {
                Timber.e(e)
            }

            true
        })
    }

    /**
     * Remove set image.

     * @param file
     */
    private fun removeSetImage(file: File?) {
        if (file == null || !file.exists()) {
            Toaster.snackShort(
                    binding.text,
                    R.string.message_cannot_found_image,
                    preferenceApplier.colorPair()
            )
            return
        }
        val successRemove = file.delete()
        if (!successRemove) {
            Toaster.snackShort(
                    binding.text,
                    R.string.message_failed_image_removal,
                    preferenceApplier.colorPair()
            )
            return
        }
        Toaster.snackShort(
                binding.text,
                R.string.message_success_image_removal,
                preferenceApplier.colorPair()
        )
        onRemoved.run()
    }
}