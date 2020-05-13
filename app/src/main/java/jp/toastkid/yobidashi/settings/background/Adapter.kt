package jp.toastkid.yobidashi.settings.background

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.SavedImageBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir

/**
 * RecyclerView's adapter.
 *
 * @param preferenceApplier Preferences wrapper.
 * @param filesDir FilesDir wrapper.
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val preferenceApplier: PreferenceApplier,
        private val filesDir: FilesDir
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = DataBindingUtil.inflate<SavedImageBinding>(
                LayoutInflater.from(parent.context),
                R.layout.saved_image,
                parent,
                false
        )
        return ViewHolder(itemBinding, preferenceApplier, this::notifyDataSetChanged)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.applyContent(filesDir.get(position))
    }

    override fun getItemCount(): Int {
        return filesDir.count
    }
}