package jp.toastkid.jitte.browser.screenshots

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ItemImageCardBinding
import jp.toastkid.jitte.libs.storage.Storeroom
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.preference.ColorPair
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * Screenshot's adapter.

 * @author toastkidjp
 */
internal class Adapter
/**
 * Initialize with [Context].
 * @param context
 */
(context: Context) : RecyclerView.Adapter<ViewHolder>() {

    /** Files dir wrapper.  */
    private val screenshots: Storeroom

    /** Layout inflater.  */
    private val inflater: LayoutInflater

    /** For using snackbar.  */
    private val colorPair: ColorPair

    init {
        screenshots = Storeroom(context, Screenshot.DIR)
        inflater = LayoutInflater.from(context)
        colorPair = PreferenceApplier(context).colorPair()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate<ItemImageCardBinding>(inflater, R.layout.item_image_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setImage(screenshots.get(position)!!)
        holder.itemView.setOnLongClickListener { v ->
            if (screenshots.remove(position)) {
                notifyItemRemoved(position)
                Toaster.snackShort(holder.itemView, R.string.done_clear, colorPair)
            }
            false
        }
    }

    override fun getItemCount(): Int {
        return screenshots.count
    }
}
