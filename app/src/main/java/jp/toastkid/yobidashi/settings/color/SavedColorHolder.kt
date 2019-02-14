package jp.toastkid.yobidashi.settings.color

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button

import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
internal class SavedColorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView: Button = itemView.findViewById<Button>(R.id.color)
    val remove: View = itemView.findViewById(R.id.color_remove)

}