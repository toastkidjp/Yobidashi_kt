package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemTabListBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Tab list adapter.

 * @author toastkidjp
 */
internal class Adapter
/**
 * Initialize with context and so on...

 * @param context
 * *
 * @param tabAdapter
 * *
 * @param closer
 */
(context: Context,
 /** Tab list model.  */
 private val tabAdapter: TabAdapter,
 /** Closing action.  */
 private val closeAction: Runnable) : RecyclerView.Adapter<ViewHolder>() {

    /** For getting Data binding object.  */
    private val inflater: LayoutInflater

    /** For snackbar and view color.  */
    private val colorPair: ColorPair

    init {
        this.inflater = LayoutInflater.from(context)
        this.colorPair = PreferenceApplier(context).colorPair()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate<ItemTabListBinding>(inflater, R.layout.item_tab_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tab = tabAdapter.getTabByIndex(position)
        holder.itemView.setOnClickListener { v ->
            tabAdapter.setIndex(position)
            closeAction.run()
        }
        holder.setTitle(tab.latest.title())
        holder.setImagePath(tab.thumbnailPath)
        holder.setCloseAction(View.OnClickListener { v -> closeAt(position) })
        holder.setColor(colorPair)
    }

    /**
     * Close tab at index.
     * @param position
     */
    private fun closeAt(position: Int) {
        tabAdapter.closeTab(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return tabAdapter.size()
    }
}
