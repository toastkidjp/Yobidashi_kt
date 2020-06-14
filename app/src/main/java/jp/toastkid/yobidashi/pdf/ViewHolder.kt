package jp.toastkid.yobidashi.pdf

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemPdfContentBinding

/**
 * PDF viewer's view holder
 *
 * @param binding [ItemPdfContentBinding]
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemPdfContentBinding)
    : RecyclerView.ViewHolder(binding.root) {

    /**
     * Set image bitmap.
     *
     * @param image [Bitmap]
     */
    fun setImage(image: Bitmap) {
        binding.image.setImageBitmap(image)
    }

    /**
     * Set indicator.
     *
     * @param current [Int]
     * @param max [Int]
     */
    fun setIndicator(current: Int, max: Int) {
        binding.pageIndicator.text = "$current / $max"
    }

}