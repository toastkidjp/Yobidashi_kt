package jp.toastkid.yobidashi.browser.tab

import android.support.v7.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemTabHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Tab item view's holder.

 * @author toastkidjp
 */
internal class TabHistoryViewHolder(private val binding: ItemTabHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setUrl(url: String) {
        binding.url.text = url
    }

    fun setImagePath(thumbnailPath: String) {
        if (thumbnailPath.isEmpty()) {
            return
        }
        ImageLoader.setImageToImageView(binding.icon, thumbnailPath)
    }

    fun setColor(pair: ColorPair) {
        //binding.close.setColorFilter(pair.bgColor())
        binding.title.setTextColor(pair.fontColor())
    }

    fun setBackgroundColor(color: Int) {
        binding.root.setBackgroundColor(color)
    }
}
