package jp.toastkid.yobidashi.search.url_suggestion

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import coil.load
import jp.toastkid.lib.view.SwipeViewHolder
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import jp.toastkid.yobidashi.libs.DateFormatHolder
import java.util.Calendar

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
        binding.icon.load(path) { size(64) }
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

    fun setTime(timeMs: Long) {
        binding.time.text =
                DateFormatHolder(binding.root.context)
                        ?.format(Calendar.getInstance().also { it.timeInMillis = timeMs }.time)
    }
}