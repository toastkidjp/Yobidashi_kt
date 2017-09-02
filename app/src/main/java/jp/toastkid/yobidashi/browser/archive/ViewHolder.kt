package jp.toastkid.yobidashi.browser.archive

import android.support.v7.widget.RecyclerView
import android.view.View

import jp.toastkid.yobidashi.databinding.ItemArchiveBinding

/**
 * View holder.

 * @author toastkidjp
 */
internal class ViewHolder
/**
 * Initialize with binding object.
 * @param binding
 */
(
        /** Data binding object.  */
        private val binding: ItemArchiveBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setText(name: String) {
        binding.text.text = name
    }

    fun setDelete(action: View.OnClickListener) {
        binding.delete.setOnClickListener(action)
    }

    fun setSubText(s: String) {
        binding.subtext.text = s
    }
}
