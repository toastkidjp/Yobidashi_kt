package jp.toastkid.yobidashi.planning_poker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

import jp.toastkid.yobidashi.R

/**
 * For using displaying selected card.
 *
 * @author toastkidjp
 */
class CardViewActivity : AppCompatActivity() {

    /**
     * Card Fragment.
     */
    private lateinit var cardFragment: CardFragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LAYOUT_ID)

        cardFragment = CardFragment()
        cardFragment.arguments = Bundle().apply {
            putString(EXTRA_KEY_CARD_TEXT, intent.getStringExtra(EXTRA_KEY_CARD_TEXT))
        }
        addFragment(cardFragment)
    }

    /**
     * Add fragment to this activity.
     *
     * @param fragment [Fragment]
     */
    private fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.commit()
    }

    companion object {

        /**
         * Card extra key.
         */
        const val EXTRA_KEY_CARD_TEXT: String = "card_text"

        /**
         * Layout ID.
         */
        private const val LAYOUT_ID: Int = R.layout.activity_transparent

        /**
         * Make [CardViewActivity]'s intent.
         *
         * @param context [Context]
         * @param text card text.
         * @return [Intent]
         */
        fun makeIntent(
                context: Context,
                text: String
        ): Intent {
            val intent = Intent(context, CardViewActivity::class.java)
            intent.putExtra(EXTRA_KEY_CARD_TEXT, text)
            return intent
        }
    }
}
