package jp.toastkid.yobidashi.planning_poker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.cardsView?.let {
            it.layoutManager = layoutManager
            it.adapter = Adapter()
            layoutManager.scrollToPosition(Adapter.medium())
            ItemTouchHelper(
                    object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP, ItemTouchHelper.UP) {
                        override fun onMove(
                                rv: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder
                        ): Boolean {
                            val fromPos = viewHolder.adapterPosition
                            val toPos = target.adapterPosition
                            it.adapter?.notifyItemMoved(fromPos, toPos)
                            (viewHolder as CardViewHolder).open()
                            return true
                        }

                        override fun onSwiped(
                                viewHolder: RecyclerView.ViewHolder,
                                direction: Int
                        ) {
                            (viewHolder as CardViewHolder).open()
                        }
                    }).attachToRecyclerView(it)
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.background?.let { ImageLoader.setImageToImageView(it, backgroundImagePath()) }
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
