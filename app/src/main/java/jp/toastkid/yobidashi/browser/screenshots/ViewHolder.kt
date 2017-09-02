package jp.toastkid.yobidashi.browser.screenshots

import android.net.Uri
import android.support.v7.widget.RecyclerView

import java.io.File

import jp.toastkid.yobidashi.databinding.ItemImageCardBinding

/**
 * Screenshot's view holder.

 * @author toastkidjp
 */
internal class ViewHolder
/**
 * Initialize with data binding object.
 * @param binding
 */
(
        /** Data binding object.  */
        private val binding: ItemImageCardBinding) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Set image file.
     * @param file
     */
    fun setImage(file: File) {
        binding.image.setImageURI(Uri.fromFile(file))
    }
}
