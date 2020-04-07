package jp.toastkid.yobidashi.search.url_suggestion

import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.databinding.ItemBookmarkBinding

/**
 * ViewHolder.
 *
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemBookmarkBinding): RecyclerView.ViewHolder(binding.root) {

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

}