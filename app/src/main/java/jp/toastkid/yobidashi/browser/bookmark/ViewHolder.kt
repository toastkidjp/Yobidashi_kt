package jp.toastkid.yobidashi.browser.bookmark

import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import coil.load
import jp.toastkid.lib.view.SwipeViewHolder
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import java.io.File

/**
 * Bookmark item [ViewHolder].
 *
 * @param binding Data Binding object
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemViewHistoryBinding)
    : RecyclerView.ViewHolder(binding.root), SwipeViewHolder {

    private val buttonMargin = binding.root.resources
        .getDimensionPixelSize(R.dimen.button_margin)

    init {
        binding.bookmark.isVisible = false
    }

    /**
     * Set text and URL.
     *
     * @param text text
     * @param url URL string
     */
    fun setText(text: String, url: String) {
        binding.title.text = text
        binding.url.isVisible = url.isNotBlank()
        binding.url.text = url
    }

    fun setTimeIfNeed(lastViewed: Long) {
        binding.time.isVisible = lastViewed != 0L
        if (binding.time.isVisible) {
            binding.time.text = DateFormat.format("yyyy/MM/dd(E)HH:mm:ss", lastViewed)
        }
    }

    /**
     * Set image with drawable ID.
     *
     * @param iconId Icon's drawable resource ID.
     */
    fun setImageId(@DrawableRes iconId: Int) {
        binding.icon.setImageResource(iconId)
    }

    /**
     * Set image with favicon path.
     *
     * @param faviconPath favicon path
     */
    fun setImage(faviconPath: String) {
        if (faviconPath.isEmpty()) {
            setDefaultIcon()
            return
        }

        binding.icon.load(File(faviconPath))
    }

    /**
     * Set default icon.
     */
    private fun setDefaultIcon() {
        binding.icon.setImageResource(R.drawable.ic_bookmark_black)
    }

    /**
     * Set action when click add-button.
     *
     * @param bookmark [Bookmark] item
     * @param onClickAdd click action
     */
    fun setOnClickAdd(bookmark: Bookmark, onClickAdd: (Bookmark) -> Unit) {
        binding.delete.setOnClickListener { onClickAdd(bookmark) }
    }

    /**
     * Set icon color filter.
     *
     * @param color Color Int
     */
    fun setIconColorFilter(@ColorInt color: Int) {
        binding.icon.setColorFilter(color)
    }

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

    override fun getFrontView(): View = binding.front

}
