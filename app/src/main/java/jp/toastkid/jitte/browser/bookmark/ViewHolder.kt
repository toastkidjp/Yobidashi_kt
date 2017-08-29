package jp.toastkid.jitte.browser.bookmark

import android.graphics.Bitmap
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.browser.bookmark.model.Bookmark
import jp.toastkid.jitte.databinding.ItemViewHistoryBinding
import jp.toastkid.jitte.libs.ImageLoader
import java.io.File

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemViewHistoryBinding)
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
                        { e -> setDefaultIcon() }
                )
    }

    private fun setDefaultIcon() {
        binding.icon.setImageResource(R.drawable.ic_bookmark_black)
    }

    fun setOnClickAdd(history: Bookmark, onClickAdd: (Bookmark) -> Unit) {
        binding.delete.setOnClickListener ({ _ -> onClickAdd(history) })
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

}
