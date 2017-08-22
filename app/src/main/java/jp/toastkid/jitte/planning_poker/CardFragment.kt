package jp.toastkid.jitte.planning_poker

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.CardItemBinding
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * Card Fragment.

 * @author toastkidjp
 */
class CardFragment : Fragment() {

    /** DataBinding object.  */
    private var binding: CardItemBinding? = null

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<CardItemBinding>(inflater!!, LAYOUT_ID, container, false)
        binding?.root?.setOnClickListener({ v ->
            Toaster.snackLong(
                    v,
                    R.string.message_confirm_back,
                    R.string.back,
                    View.OnClickListener{ view -> activity.finish() },
                    PreferenceApplier(v.context).colorPair()
            )
        })
        return binding!!.root
    }

    /**
     * Set card's text.
     * @param text card's text
     */
    fun setText(text: String) {
        binding!!.cardText.text = text
        if (text.length >= 3) {
            binding!!.cardText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120f)
        }
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.card_item
    }

}
