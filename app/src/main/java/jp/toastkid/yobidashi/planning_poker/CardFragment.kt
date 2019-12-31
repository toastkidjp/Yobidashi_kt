package jp.toastkid.yobidashi.planning_poker

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.CardItemBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Card Fragment.
 *
 * @author toastkidjp
 */
class CardFragment : Fragment() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: CardItemBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.root.setOnClickListener { v ->
            Toaster.snackLong(
                    v,
                    R.string.message_confirm_back,
                    R.string.back,
                    View.OnClickListener{ activity?.finish() },
                    PreferenceApplier(v.context).colorPair()
            )
        }
        val arguments = arguments ?: Bundle()
        if (arguments.containsKey(CardViewActivity.EXTRA_KEY_CARD_TEXT)) {
            arguments.getString(CardViewActivity.EXTRA_KEY_CARD_TEXT)
                    ?.let { setText(it) }
        }
        return binding.root
    }

    /**
     * Set card's text.
     *
     * @param text card's text
     */
    private fun setText(text: String) {
        binding.cardText.text = text
        if (text.length >= 3) {
            binding.cardText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120f)
        }
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.item_planning_poker
    }

}
