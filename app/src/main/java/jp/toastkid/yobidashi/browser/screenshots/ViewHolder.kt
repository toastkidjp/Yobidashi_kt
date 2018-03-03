package jp.toastkid.yobidashi.browser.screenshots

import android.graphics.Bitmap
import android.support.annotation.ColorInt
import android.support.v7.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemScreenshotBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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
        private val binding: ItemScreenshotBinding) : RecyclerView.ViewHolder(binding.root) {

    private val dateFormatHolder = object : ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat =
                SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault())
    }

    /**
     * Set image bitmap.
     * @param bitmap
     */
    fun setImage(bitmap: Bitmap) {
        binding.image.setImageBitmap(bitmap)
    }

    /**
     * Set icon's color.
     * @param color Color Int
     */
    fun setIconColor(@ColorInt color: Int) {
        binding.delete.setColorFilter(color)
        binding.share.setColorFilter(color)
    }

    fun setDeleteAction(function: () -> Unit) {
        binding.delete.setOnClickListener { function() }
    }

    fun setShareAction(function: () -> Unit) {
        binding.share.setOnClickListener { function() }
    }

    fun setTimestamp(lastModified: Long) {
        binding.datetime.text = dateFormatHolder.get().format(lastModified)
    }

}
