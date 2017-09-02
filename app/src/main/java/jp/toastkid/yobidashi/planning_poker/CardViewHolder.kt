package jp.toastkid.yobidashi.planning_poker

import android.support.v7.widget.RecyclerView
import android.util.TypedValue

import jp.toastkid.yobidashi.databinding.CardItemBinding

/**
 * @author toastkidjp
 */
internal class CardViewHolder(private val binding: CardItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String) {
        binding.cardText.text = text
        if (text.codePointCount(0, text.length) >= 3) {
            binding.cardText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120f)
        }
    }

    fun open() {
        val context = binding.root.context
        context.startActivity(
                CardViewActivity.makeIntent(context, binding.cardText.text.toString()))
    }
}