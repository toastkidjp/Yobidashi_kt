package jp.toastkid.yobidashi.settings.background

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
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
 * @param binding Binding object.
 * @param preferenceApplier Preferences wrapper.
 * @param onRemoved Action on removed.
 *
 * @author toastkidjp
 */
internal class ViewHolder(
        private val binding: SavedImageBinding,
        private val preferenceApplier: PreferenceApplier,
        private val onRemoved: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Apply file content.
     *
     * @param f background image file
     */
    fun applyContent(f: File?) {
        if (f == null) {
            return
        }

        ImageLoader.setImageToImageView(this.binding.image, f.path)
        this.binding.text.text = f.name
        this.binding.remove.setOnClickListener { removeSetImage(f) }
        this.binding.root.setOnClickListener {
            preferenceApplier.backgroundImagePath = f.path
            Toaster.snackShort(
                    binding.image,
                    R.string.message_change_background_image,
                    preferenceApplier.colorPair()
            )
        }
        this.binding.root.setOnLongClickListener { v ->
            try {
                val context = v.context
                if (context is FragmentActivity) {
                    ImageDialogFragment.withUrl(f.toURI().toString())
                            .show(
                                    context.supportFragmentManager,
                                    ImageDialogFragment::class.java.simpleName
                            )
                }
            } catch (e: IOException) {
                Timber.e(e)
            }

            true
        }
    }

    /**
     * Remove set image.
     *
     * @param file Image file
     */
    private fun removeSetImage(file: File?) {
        if (file == null || !file.exists()) {
            snack(R.string.message_cannot_found_image)
            return
        }
        val successRemove = file.delete()
        if (!successRemove) {
            snack(R.string.message_failed_image_removal)
            return
        }
        snack(R.string.message_success_image_removal)
        onRemoved()
    }

    /**
     * Show [Snackbar] with specified message resource.
     *
     * @param messageId Message ID
     */
    private fun snack(@StringRes messageId: Int) {
        Toaster.snackShort(
                binding.text,
                messageId,
                preferenceApplier.colorPair()
        )
    }
}