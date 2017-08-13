package jp.toastkid.yobidashi.search.favorite

import android.support.annotation.DrawableRes
import android.support.v7.widget.AppCompatDrawableManager
import android.support.v7.widget.RecyclerView
import android.view.View

import jp.toastkid.yobidashi.databinding.FavoriteSearchItemBinding

/**
 * Favorite Search item views holder.
 * @author toastkidjp
 */
internal class FavoriteSearchHolder(private val binding: FavoriteSearchItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setImageId(@DrawableRes iconId: Int) {
        binding.favoriteSearchImage.setImageDrawable(
                AppCompatDrawableManager.get()
                        .getDrawable(binding.favoriteSearchImage.context, iconId))
    }

    fun setText(query: String) {
        binding.favoriteSearchText.text = query
    }

    fun setRemoveAction(listener: View.OnClickListener) {
        binding.favoriteSearchDelete.setOnClickListener(listener)
    }

    fun setClickAction(listener: View.OnClickListener) {
        itemView.setOnClickListener(listener)
    }
}