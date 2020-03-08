package jp.toastkid.yobidashi.planning_poker

import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemPlanningPokerBinding

/**
 * @author toastkidjp
 */
internal class CardViewHolder(private val binding: ItemPlanningPokerBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String) {
        binding.cardText.text = text
        if (text.codePointCount(0, text.length) >= 3) {
            binding.cardText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120f)
        }
    }

    // TODO should use ViewModel.
    fun open() {
        (binding.root.context as? PlanningPokerActivity)
                ?.setCard(binding.cardText.text.toString())
    }
}