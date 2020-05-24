package jp.toastkid.yobidashi.planning_poker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            LAYOUT_ID,
                            parent,
                            false
                    )
            )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = Suite.values()[position % Suite.values().size].text()
        holder.setText(text)
    }

    override fun getItemCount(): Int = MAXIMUM_SIZE

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_planning_poker

        /**
         * Maximum size.
         */
        private val MAXIMUM_SIZE = Suite.values().size * 20

        /**
         * Medium.
         */
        private val MEDIUM = MAXIMUM_SIZE / 2

        /**
         * Return medium number.
         */
        fun medium(): Int = MEDIUM
    }
}