package jp.toastkid.jitte.browser.tab

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ItemTabListBinding
import jp.toastkid.jitte.libs.preference.ColorPair
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * Tab list adapter.
 * Initialize with context and so on...
 * @param context
 * @param tabAdapter Tab list model
 * @param closeAction Closing action
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val context: Context,
        private val tabAdapter: TabAdapter,
        private val closeAction: () -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /** For getting Data binding object.  */
    private val inflater: LayoutInflater

    /** For snackbar and view color.  */
    private val colorPair: ColorPair

    /** Current index. */
    private var index = -1;

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
        holder.itemView.setOnClickListener { _ ->
            tabAdapter.setIndexByTab(tab)
            closeAction()
        }
        holder.setTitle(tab.latest.title())
        holder.setImagePath(tab.thumbnailPath)
        holder.setCloseAction(View.OnClickListener { _ -> closeAt(tabAdapter.indexOf(tab)) })
        holder.setColor(colorPair)
        holder.setBackgroundColor(
                if (index == position) {
                    ColorUtils.setAlphaComponent(colorPair.bgColor(), 128)
                } else {
                    Color.TRANSPARENT
                }
        );
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

    fun setCurrentIndex(newIndex: Int) {
        index = newIndex
    }

}
