package jp.toastkid.yobidashi.barcode.detection

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemDetectionResultBinding

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemDetectionResultBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setBitmap(bitmap: Bitmap) {
        binding.image.setImageBitmap(bitmap)
    }

    fun setCategory(category: String) {
        binding.category.setText(category)
    }
}
