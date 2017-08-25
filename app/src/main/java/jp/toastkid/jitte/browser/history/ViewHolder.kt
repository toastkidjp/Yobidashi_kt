package jp.toastkid.jitte.browser.history


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ItemViewHistoryBinding

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemViewHistoryBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String, url: String) {
        binding.title.text = text
        binding.url.text = url
    }

    fun setImage(faviconPath: String): Disposable {
        if (faviconPath.isEmpty()) {
            setDefaultIcon()
            return Disposables.empty()
        }
        return Single.create<Bitmap>{ e -> e.onSuccess(BitmapFactory.decodeFile(faviconPath))}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { it -> binding.icon.setImageBitmap(it) },
                        { e -> setDefaultIcon() }
                )
    }

    private fun setDefaultIcon() {
        binding.icon.setImageResource(R.drawable.ic_history_black)
    }

    fun setOnClickAdd(history: ViewHistory, onClickAdd: (ViewHistory) -> Unit) {
        binding.delete.setOnClickListener ({ _ ->
            onClickAdd(history)
        })
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

}
