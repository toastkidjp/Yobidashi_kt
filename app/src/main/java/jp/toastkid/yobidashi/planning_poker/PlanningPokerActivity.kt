package jp.toastkid.yobidashi.planning_poker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityPlanningPokerBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Planning Poker Fragment.
 *
 * @author toastkidjp
 */
class PlanningPokerActivity : AppCompatActivity() {

    /**
     * DataBinding object.
     */
    private var binding: ActivityPlanningPokerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                0,
                0,
                android.R.anim.slide_out_right
        )
        transaction.add(R.id.content, CardListFragment())
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        binding?.background?.let { ImageLoader.setImageToImageView(it, backgroundImagePath()) }
    }

    fun setCard(text: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
        transaction.add(R.id.content, CardFragment.makeWithNumber(text))
        transaction.addToBackStack(CardFragment::class.java.canonicalName)
        transaction.commit()
    }

    private fun backgroundImagePath() = PreferenceApplier(this).backgroundImagePath

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_planning_poker

        /**
         * Make this activity launcher intent.
         *
         * @param context
         * @return
         */
        fun makeIntent(context: Context): Intent  =
                Intent(context, PlanningPokerActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

}
