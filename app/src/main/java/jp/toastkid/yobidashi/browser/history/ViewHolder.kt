package jp.toastkid.yobidashi.browser.history


import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import jp.toastkid.lib.view.SwipeViewHolder
import java.io.File

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemViewHistoryBinding)
    : RecyclerView.ViewHolder(binding.root), SwipeViewHolder {

    private val buttonMargin = binding.root.resources
            .getDimensionPixelSize(R.dimen.button_margin)

    fun setText(text: String, url: String, time: String) {
        binding.title.text = text
        binding.url.text   = url
        binding.time.text  = time
    }

    fun setImage(faviconPath: String) {
        if (faviconPath.isEmpty()) {
            setDefaultIcon()
            return
        }
        Glide.with(binding.icon)
                .load(File(faviconPath))
                .into(binding.icon)
    }

    private fun setDefaultIcon() {
        binding.icon.setImageResource(R.drawable.ic_history_black)
    }

    fun setOnClickBookmark(history: ViewHistory) {
        binding.bookmark.setOnClickListener {
            val context = binding.root.context
            if (context is FragmentActivity) {
                AddBookmarkDialogFragment.make(history.title, history.url).show(
                        context.supportFragmentManager,
                        AddBookmarkDialogFragment::class.java.simpleName
                )
            }
        }
    }

    fun setOnClickAdd(history: ViewHistory, onClickAdd: (ViewHistory) -> Unit) {
        binding.delete.setOnClickListener { onClickAdd(history) }
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
