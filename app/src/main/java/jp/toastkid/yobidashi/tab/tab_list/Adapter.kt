package jp.toastkid.yobidashi.tab.tab_list

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.TabAdapter

/**
 * WebTab list adapter.
 * Initialize with context and so on...
 *
 * @param context
 * @param tabAdapter WebTab list model
 * @param closeAction Closing action
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val context: Context,
        private val tabAdapter: TabAdapter,
        private val closeAction: () -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /**
     * For getting Data binding object.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * For snackbar and view color.
     */
    private val colorPair: ColorPair = PreferenceApplier(context).colorPair()

    /**
     * Current index.
     */
    private var index = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(inflater, R.layout.item_tab_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tab = tabAdapter.getTabByIndex(position)
        holder.itemView.setOnClickListener {
            tabAdapter.replace(tab)
            closeAction()
        }
        holder.setImagePath(tab.thumbnailPath)
        holder.setTitle(tab.title())
        holder.setCloseAction(View.OnClickListener { closeAt(tabAdapter.indexOf(tab)) })
        holder.setColor(colorPair)
        holder.setBackgroundColor(
                if (index == position) {
                    ColorUtils.setAlphaComponent(colorPair.bgColor(), 128)
                } else {
                    Color.TRANSPARENT
                }
        )
    }

    /**
     * Close tab at index.
     * @param position
     */
    private fun closeAt(position: Int) {
        tabAdapter.closeTab(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = tabAdapter.size()

    /**
     * Set current index.
     */
    fun setCurrentIndex(newIndex: Int) {
        index = newIndex
    }

    fun swap(from: Int, to: Int) {
        tabAdapter.swap(from, to)
        notifyItemMoved(from, to)
    }
}
