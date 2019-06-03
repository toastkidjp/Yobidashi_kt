package jp.toastkid.yobidashi.browser.archive

import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import jp.toastkid.yobidashi.databinding.ItemArchiveBinding

/**
 * View holder.
 *
 * Initialize with binding object.
 * @param binding Data binding object.
 *
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemArchiveBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(name: String) {
        binding.text.text = name
    }

    fun setDelete(action: View.OnClickListener) {
        binding.delete.setOnClickListener(action)
    }

    fun setSubText(s: String) {
        binding.subtext.text = s
    }

    fun setIconColor(@ColorInt color: Int) {
        binding.icon.setColorFilter(color)
        binding.delete.setColorFilter(color)
    }
}
