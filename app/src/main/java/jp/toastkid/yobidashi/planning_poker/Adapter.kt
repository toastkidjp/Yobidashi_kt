package jp.toastkid.yobidashi.planning_poker

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.toastkid.yobidashi.R

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter : RecyclerView.Adapter<CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CardViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_planning_poker,
                            parent,
                            false
                    )
            )

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val text = Suite.values()[position % Suite.values().size].text()
        holder.setText(text)
        holder.itemView.setOnClickListener { holder.open() }
    }

    override fun getItemCount(): Int = MAXIMUM_SIZE

    companion object {

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