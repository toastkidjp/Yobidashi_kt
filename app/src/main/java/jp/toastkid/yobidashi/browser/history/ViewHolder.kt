package jp.toastkid.yobidashi.browser.history


import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import java.io.File

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemViewHistoryBinding)
    : RecyclerView.ViewHolder(binding.root) {

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

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

}
