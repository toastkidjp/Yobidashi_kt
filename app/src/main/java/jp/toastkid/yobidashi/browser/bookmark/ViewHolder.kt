package jp.toastkid.yobidashi.browser.bookmark

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
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
    fun setImage(faviconPath: String): Disposable {
        if (faviconPath.isEmpty()) {
            setDefaultIcon()
            return Disposables.empty()
        }
        return Single.fromCallable { File(faviconPath) }
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
