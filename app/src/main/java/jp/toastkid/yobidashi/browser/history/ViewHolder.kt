package jp.toastkid.yobidashi.browser.history


import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.Bookmarks
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
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

    fun setImage(faviconPath: String): Disposable {
        if (faviconPath.isEmpty()) {
            setDefaultIcon()
            return Disposables.empty()
        }
        return Single.create<File>{ e -> e.onSuccess(File(faviconPath))}
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { ImageLoader.loadBitmap(binding.root.context, Uri.fromFile(it)) as Bitmap }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { binding.icon.setImageBitmap(it) },
                        { e -> setDefaultIcon() }
                )
    }

    private fun setDefaultIcon() {
        binding.icon.setImageResource(R.drawable.ic_history_black)
    }

    fun setOnClickBookmark(history: ViewHistory) {
        binding.bookmark.setOnClickListener ({ _ ->
            val context = binding.root.context
            AlertDialog.Builder(context)
                    .setTitle(R.string.title_add_bookmark)
                    .setMessage(R.string.message_add_bookmark)
                    .setPositiveButton(R.string.ok, {d, i -> BookmarkInsertion(
                            context,
                            history.title,
                            history.url,
                            Bookmarks.makeFaviconUrl(context, history.url),
                            "root",
                            false
                        ).insert()
                        d.dismiss()
                    })
                    .setNegativeButton(R.string.cancel, {d, i -> d.cancel()})
                    .setCancelable(true)
                    .show()
        })
    }

    fun setOnClickAdd(history: ViewHistory, onClickAdd: (ViewHistory) -> Unit) {
        binding.delete.setOnClickListener ({ _ -> onClickAdd(history) })
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

}
