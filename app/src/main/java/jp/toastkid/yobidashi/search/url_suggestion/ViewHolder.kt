package jp.toastkid.yobidashi.search.url_suggestion

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import jp.toastkid.yobidashi.libs.view.SwipeViewHolder

/**
 * ViewHolder.
 *
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemViewHistoryBinding):
        RecyclerView.ViewHolder(binding.root), SwipeViewHolder {

    private val buttonMargin = binding.root.resources
            .getDimensionPixelSize(R.dimen.button_margin)

    /**
     * Set title.
     *
     * @param title
     */
    fun setTitle(title: String) {
        binding.title.setText(title)
    }

    /**
     * Set URL.
     *
     * @param url
     */
    fun setUrl(url: String) {
        binding.url.setText(url)
    }

    fun setIconResource(@DrawableRes iconId: Int) {
        binding.icon.setImageResource(iconId)
    }

    fun setIconFromPath(path: String) {
        Glide.with(itemView.context)
                .load(path)
                .override(64)
                .into(binding.icon)
    }

    /**
     * Set on click listener.
     *
     * @param listener
     */
    fun setOnClick(listener: View.OnClickListener) {
        binding.root.setOnClickListener(listener)
    }

    /**
     * Set on long click listener.
     *
     * @param longClickListener
     */
    fun setOnLongClick(longClickListener: View.OnLongClickListener) {
        binding.root.setOnLongClickListener(longClickListener)
    }

    /**
     * Set on click listener to delete button.
     *
     * @param listener
     */
    fun setDelete(listener: View.OnClickListener) {
        binding.delete.setOnClickListener(listener)
    }


    override fun getFrontView(): View = binding.front

    override fun isButtonVisible(): Boolean = binding.delete.isVisible

    override fun showButton() {
        binding.delete.visibility = View.VISIBLE
        updateRightMargin(buttonMargin)
    }

    override fun hideButton() {
        binding.delete.visibility = View.INVISIBLE
        updateRightMargin(0)
    }

    private fun updateRightMargin(margin: Int) {
        val marginLayoutParams = binding.front.layoutParams as? ViewGroup.MarginLayoutParams
        marginLayoutParams?.rightMargin = margin
        binding.front.layoutParams = marginLayoutParams
        marginLayoutParams?.updateMargins()
    }
}