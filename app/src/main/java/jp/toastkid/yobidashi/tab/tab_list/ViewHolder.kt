package jp.toastkid.yobidashi.tab.tab_list

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemTabListBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair
import java.io.File

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

        Glide.with(binding.image)
                .load(File(thumbnailPath).toURI().toString().toUri())
                .override(binding.image.measuredWidth, binding.image.measuredHeight)
                .into(binding.image)
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
