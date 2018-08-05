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

 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemTabListBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setTitle(title: String?) {
        binding.title.text = title ?: ""
    }

    fun setImagePath(thumbnailPath: String) {
        binding.image.setColorFilter(Color.TRANSPARENT)
        if (thumbnailPath.isEmpty()) {
            binding.image.setImageResource(R.mipmap.ic_launcher_round)
            return
        }
        ImageLoader.setImageToImageView(binding.image, thumbnailPath)
    }

    fun setEditorImage(@ColorInt color: Int) {
        binding.image.setImageResource(R.drawable.ic_edit)
        binding.image.setColorFilter(color)
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
