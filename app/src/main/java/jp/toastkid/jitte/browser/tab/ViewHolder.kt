package jp.toastkid.jitte.browser.tab

import android.support.v7.widget.RecyclerView
import android.view.View
import jp.toastkid.jitte.databinding.ItemTabListBinding
import jp.toastkid.jitte.libs.ImageLoader
import jp.toastkid.jitte.libs.preference.ColorPair

/**
 * Tab item view's holder.

 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemTabListBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setImagePath(thumbnailPath: String) {
        if (thumbnailPath.isEmpty()) {
            return
        }
        ImageLoader.setImageToImageView(binding.image, thumbnailPath)
    }

    fun setCloseAction(listener: View.OnClickListener) {
        binding.close.setOnClickListener(listener)
    }

    fun setColor(pair: ColorPair) {
        binding.close.setColorFilter(pair.bgColor())
        binding.title.setTextColor(pair.fontColor())
        binding.title.setBackgroundColor(pair.bgColor())
    }

    fun close() {
        binding.close.callOnClick()
    }

    fun setBackgroundColor(color: Int) {
        binding.root.setBackgroundColor(color)
    }
}
