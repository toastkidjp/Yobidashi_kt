package jp.toastkid.yobidashi.planning_poker

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.CardItemBinding

/**
 * RecyclerView's adapter.

 * @author toastkidjp
 */
internal class Adapter : RecyclerView.Adapter<CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
                DataBindingUtil.inflate<CardItemBinding>(
                        LayoutInflater.from(parent.context),
                        R.layout.card_item,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val text = Suite.values()[position % Suite.values().size].text()
        holder.setText(text)
        holder.itemView.setOnClickListener { v -> holder.open() }
    }

    override fun getItemCount(): Int {
        return MAXIMUM_SIZE
    }

    companion object {

        /** Maximum size.  */
        private val MAXIMUM_SIZE = Suite.values().size * 20

        /** Medium.  */
        private val MEDIUM = MAXIMUM_SIZE / 2

        fun medium(): Int {
            return MEDIUM
        }
    }
}