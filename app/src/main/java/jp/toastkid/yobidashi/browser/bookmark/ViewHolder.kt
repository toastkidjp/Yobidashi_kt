package jp.toastkid.yobidashi.browser.bookmark

import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.databinding.ItemBookmarkBinding
import java.io.File

/**
 * Bookmark item [ViewHolder].
 *
 * @param binding Data Binding object
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemBookmarkBinding)
    : RecyclerView.ViewHolder(binding.root) {

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

        Glide.with(binding.icon)
                .load(File(faviconPath))
                .into(binding.icon)
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

}
