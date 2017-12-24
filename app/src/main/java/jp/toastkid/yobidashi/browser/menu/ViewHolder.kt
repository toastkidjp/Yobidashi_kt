package jp.toastkid.yobidashi.browser.menu

import android.graphics.Color
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.view.View

import jp.toastkid.yobidashi.databinding.ItemBrowserMenuBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Menu's view holder.

 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemBrowserMenuBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setText(@StringRes titleId: Int) {
        binding.text.setText(titleId)
    }

    fun setImage(@DrawableRes iconId: Int) {
        binding.image.setImageResource(iconId)
    }

    fun setColorPair(pair: ColorPair, useIconColorFilter: Boolean) {
        itemView.setBackgroundColor(pair.bgColor())
        binding.text.setTextColor(pair.fontColor())
        binding.image.setColorFilter(
                if (useIconColorFilter) { pair.fontColor() } else { Color.TRANSPARENT }
        )
    }

    fun setOnClick(onClick: View.OnClickListener) {
        itemView.setOnClickListener(onClick)
    }
}
