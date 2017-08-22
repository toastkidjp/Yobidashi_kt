package jp.toastkid.jitte.settings.background

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.SavedImageBinding
import jp.toastkid.jitte.libs.storage.Storeroom
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * RecyclerView's adapter.

 * @author toastkidjp
 */
internal class Adapter
/**

 * @param preferenceApplier
 * *
 * @param storeroom
 */
(
        /** Preferences wrapper.  */
        private val preferenceApplier: PreferenceApplier,
        /** FilesDir wrapper.  */
        private val storeroom: Storeroom) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = DataBindingUtil.inflate<SavedImageBinding>(
                LayoutInflater.from(parent.context),
                R.layout.saved_image,
                parent,
                false
        )
        return ViewHolder(itemBinding, preferenceApplier, Runnable { this.notifyDataSetChanged() })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.applyContent(storeroom.get(position)!!)
    }

    override fun getItemCount(): Int {
        return storeroom.count
    }
}