package jp.toastkid.yobidashi.pdf

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import jp.toastkid.yobidashi.databinding.ItemPdfContentBinding

/**
 * @author toastkidjp
 */
class ViewHolder(val binding: ItemPdfContentBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setImage(image: Bitmap) {
        binding.image.setImageBitmap(image)
    }

    fun setIndicator(current: Int, max: Int) {
        binding.pageIndicator.text = "$current / $max"
    }

    fun setOnLongTap(onLongClickListener: View.OnLongClickListener) {
        binding.image.setOnLongClickListener(onLongClickListener)
    }
}