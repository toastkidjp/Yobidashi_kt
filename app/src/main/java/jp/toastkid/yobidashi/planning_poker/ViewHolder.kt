package jp.toastkid.yobidashi.planning_poker

import android.util.TypedValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemPlanningPokerBinding

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemPlanningPokerBinding)
    : RecyclerView.ViewHolder(binding.root) {

    private var viewModel: CardListFragmentViewModel? = null

    init {
        binding.viewHolder = this
        viewModel = (binding.root.context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(CardListFragmentViewModel::class.java)
        }
    }

    fun setText(text: String) {
        binding.cardText.text = text
        if (text.codePointCount(0, text.length) >= 3) {
            binding.cardText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120f)
        }
    }

    fun open() {
        viewModel?.nextCard(binding.cardText.text.toString())
    }

}