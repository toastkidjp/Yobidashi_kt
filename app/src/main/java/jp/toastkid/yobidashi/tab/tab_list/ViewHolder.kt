package jp.toastkid.yobidashi.tab.tab_list

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemTabListBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * WebTab item view's holder.
 *
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemTabListBinding) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Set tab's title.
     *
     * @param title tab's title(Nullable)
     */
    fun setTitle(title: String?) {
        binding.title.text = title ?: ""
    }

    /**
     * Set thumbnail path.
     *
     * @param thumbnailPath Path of thumbnail.
     */
    fun setImagePath(thumbnailPath: String) {
        binding.image.setColorFilter(Color.TRANSPARENT)
        if (thumbnailPath.isEmpty()) {
            binding.image.setImageResource(R.mipmap.ic_launcher_round)
            return
        }
        ImageLoader.setImageToImageView(binding.image, thumbnailPath)
    }

    /**
     * Set close action.
     *
     * @param listener [View.OnClickListener]
     */
    fun setCloseAction(listener: View.OnClickListener) {
        binding.close.setOnClickListener(listener)
    }

    /**
     * Set color with [ColorPair].
     *
     * @param pair [ColorPair]
     */
    fun setColor(pair: ColorPair) {
        binding.close.setColorFilter(pair.bgColor())
        binding.title.setTextColor(pair.fontColor())
        binding.title.setBackgroundColor(pair.bgColor())
    }

    /**
     * Set background color with @ColorInt
     *
     * @param color ColorInt
     */
    fun setBackgroundColor(@ColorInt color: Int) {
        binding.root.setBackgroundColor(color)
    }

    /**
     * Close action.
     */
    fun close() {
        binding.close.callOnClick()
    }

}
