package jp.toastkid.jitte.settings.color

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button

import jp.toastkid.jitte.R

/**
 * @author toastkidjp
 */
internal class SavedColorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView: Button
    val remove: View

    init {
        textView = itemView.findViewById(R.id.color) as Button
        remove = itemView.findViewById(R.id.color_remove)
    }
}