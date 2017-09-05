package jp.toastkid.yobidashi.browser.tab

import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.databinding.ItemTabHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Tab item view's holder.
 *
 * @author toastkidjp
 */
internal class TabHistoryViewHolder(private val binding: ItemTabHistoryBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setUrl(url: String) {
        binding.url.text = url
    }

    fun setImagePath(thumbnailPath: String) {
        if (thumbnailPath.isEmpty()) {
            return
        }
        ImageLoader.setImageToImageView(binding.icon, thumbnailPath)
    }

    fun setBackgroundColor(color: Int) {
        binding.root.setBackgroundColor(color)
    }

    fun setBookmarkAction(title: String, url: String, faviconPath: String) {
        binding.bookmark.setOnClickListener { _ ->
            val context = binding.root.context
            AlertDialog.Builder(context)
                    .setTitle(R.string.title_add_bookmark)
                    .setMessage(R.string.message_add_bookmark)
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel, { d, i -> d.cancel()})
                    .setPositiveButton(R.string.title_add, { d, i ->
                        BookmarkInsertion(context, title, url, faviconPath, "root").insert()
                        Toaster.snackShort(
                                binding.root,
                                context.getString(R.string.message_done_added_bookmark),
                                PreferenceApplier(context).colorPair()
                        )
                        d.dismiss()
                    })
                    .show()
        }
    }
}
