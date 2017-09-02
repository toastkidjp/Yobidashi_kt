package jp.toastkid.yobidashi.planning_poker

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityPlanningPokerBinding
import jp.toastkid.yobidashi.libs.ImageLoader

/**
 * Planning Poker Fragment.

 * @author toastkidjp
 */
class PlanningPokerActivity : BaseActivity() {

    /** DataBinding object.  */
    private var binding: ActivityPlanningPokerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityPlanningPokerBinding>(this, LAYOUT_ID)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        if (binding?.cardsView == null) {
            return
        }
        binding?.cardsView?.layoutManager = layoutManager
        binding?.cardsView?.adapter = Adapter()
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
                        binding!!.cardsView.adapter.notifyItemMoved(fromPos, toPos)
                        (viewHolder as CardViewHolder).open()
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        (viewHolder as CardViewHolder).open()
                    }
                }).attachToRecyclerView(binding!!.cardsView)
    }

    override fun onResume() {
        super.onResume()
        ImageLoader.setImageToImageView(binding!!.background, backgroundImagePath)
    }

    public override fun titleId(): Int {
        return R.string.title_planning_poker
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_planning_poker

        /**
         * Make this activity launcher intent.
         * @param context
         * *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, PlanningPokerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }

}
