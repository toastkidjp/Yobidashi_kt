package jp.toastkid.yobidashi.search.url_suggestion

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.history.ViewHistory

/**
 * URL suggestion module's adapter.
 *
 * @param context [Context]
 * @param browseCallback
 * @param browseBackgroundCallback
 * @author toastkidjp
 */
class Adapter(
        context: Context,
        private val browseCallback: (String) -> Unit,
        private val browseBackgroundCallback: (String) -> Unit
): RecyclerView.Adapter<ViewHolder>() {

    /**
     * Item list.
     */
    private val suggestions: MutableList<ViewHistory> = mutableListOf()

    /**
     * Layout inflater.
     */
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val holder = ViewHolder(DataBindingUtil.inflate(
                layoutInflater, R.layout.item_bookmark, parent, false
        ))
        return holder;
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val item: ViewHistory = suggestions.get(position)
        holder?.setTitle(item.title)
        holder?.setUrl(item.url)
        holder?.setOnClick(View.OnClickListener { browseCallback(item.url) })
        holder?.setOnLongClick(View.OnLongClickListener {
            browseBackgroundCallback(item.url)
            true
        })
    }

    override fun getItemCount(): Int = suggestions.size

    /**
     * Add item to list.
     *
     * @param item
     */
    fun add(item: ViewHistory?) {
        item?.let { suggestions.add(it) }
    }

    /**
     * Clear items.
     */
    fun clear() {
        suggestions.clear()
    }

    /**
     * Return is not empty for controlling visibility.
     */
    fun isNotEmpty(): Boolean = suggestions.isNotEmpty()

}