package jp.toastkid.yobidashi.browser.bookmark

import android.graphics.Bitmap
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.databinding.ItemBookmarkBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import timber.log.Timber
import java.io.File

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemBookmarkBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String, url: String) {
        binding.title.text = text
        binding.url.text = url
    }

    fun setImageId(@DrawableRes iconId: Int) {
        binding.icon.setImageResource(iconId)
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
                        { e ->
                            Timber.e(e)
                            setDefaultIcon()
                        }
                )
    }

    private fun setDefaultIcon() {
        binding.icon.setImageResource(
                R.drawable.ic_bookmark_black
        )
    }

    fun setOnClickAdd(history: Bookmark, onClickAdd: (Bookmark) -> Unit) {
        binding.delete.setOnClickListener ({ _ -> onClickAdd(history) })
    }

}
