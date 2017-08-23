package jp.toastkid.jitte.search.history

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.view.MenuItem
import jp.toastkid.jitte.BaseActivity
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ActivitySearchHistoryBinding
import jp.toastkid.jitte.libs.ImageLoader
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.db.DbInitter
import jp.toastkid.jitte.search.SearchAction

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchHistoryBinding

    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivitySearchHistoryBinding>(this, LAYOUT_ID)
        val relation = DbInitter.get(this).relationOfSearchHistory()

        binding.historiesView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> SearchAction(this, history.category as String, history.query as String).invoke()},
                { history -> Toaster.snackShort(binding.root, history.query as String, colorPair()) }
        )
        binding.historiesView.adapter = adapter
        binding.historiesView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return false
            }
        }
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        val fromPos = viewHolder.adapterPosition
                        val toPos = target.adapterPosition
                        adapter.notifyItemMoved(fromPos, toPos)
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        if (direction != ItemTouchHelper.RIGHT) {
                            return
                        }
                        adapter.removeAt(viewHolder.adapterPosition)
                    }
                }).attachToRecyclerView(binding.historiesView)

        initToolbar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.search_history)
    }

    override fun onResume() {
        super.onResume()

        if (adapter.itemCount == 0) {
            Toaster.tShort(this, getString(R.string.message_none_search_histories))
            finish()
        }

        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.close) {
            finish()
            return true
        }
        if (itemId == R.id.clear) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.title_clear_cache)
                    .setMessage(Html.fromHtml(getString(R.string.confirm_clear_all_settings)))
                    .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                    .setPositiveButton(R.string.ok) { d, i ->
                        adapter.clearAll{Toaster.snackShort(binding.root, R.string.done_clear, colorPair())}
                        d.dismiss()
                        finish()
                    }
                    .setCancelable(true)
                    .show()
            return true
        }
        return super.clickMenu(item)
    }

    override fun titleId(): Int = R.string.title_search_history

    companion object {
        @LayoutRes const val LAYOUT_ID: Int = R.layout.activity_search_history

        fun makeIntent(context: Context): Intent {
            val intent: Intent = Intent(context, SearchHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}