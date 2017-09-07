package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.databinding.ItemTabHistoryBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Tab history adapter.
 * Initialize with context and so on...
 *
 * @param context
 * @param tab Tab list model
 * @param closeAction Closing action
 *
 * @author toastkidjp
 */
internal class TabHistoryAdapter(
        private val context: Context,
        private val tab: Tab,
        private val clickCallback: (Int) -> Unit
) : RecyclerView.Adapter<TabHistoryViewHolder>() {

    /** For getting Data binding object.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /** For snackbar and view color.  */
    private val colorPair: ColorPair = PreferenceApplier(context).colorPair()

    /** For extracting favicon path. */
    private val faviconApplier = FaviconApplier(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHistoryViewHolder {
        return TabHistoryViewHolder(DataBindingUtil.inflate<ItemTabHistoryBinding>(
                inflater, R.layout.item_tab_history, parent, false))
    }

    override fun onBindViewHolder(holder: TabHistoryViewHolder, position: Int) {
        val history = tab.histories.get(position)
        holder.itemView.setOnClickListener { _ -> clickCallback(position) }
        holder.setTitle(history.title())
        holder.setUrl(history.url())
        holder.setBookmarkAction(
                history.title(), history.url(), faviconApplier.makePath(history.url()))
        holder.setBackgroundColor(
                if (tab.currentIndex() == position) {
                    ColorUtils.setAlphaComponent(colorPair.bgColor(), 128)
                } else {
                    Color.TRANSPARENT
                }
        );
    }

    override fun getItemCount(): Int = tab.histories.size

}
