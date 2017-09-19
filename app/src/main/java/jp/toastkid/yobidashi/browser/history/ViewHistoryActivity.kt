package jp.toastkid.yobidashi.browser.history

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.view.MenuItem
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityViewHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller

/**
 * @author toastkidjp
 */
class ViewHistoryActivity: BaseActivity() {

    private lateinit var binding: ActivityViewHistoryBinding

    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityViewHistoryBinding>(this, LAYOUT_ID)
        val relation = DbInitter.init(this).relationOfViewHistory()

        binding.historiesView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> Toaster.snackShort(binding.root, history.title, colorPair()) }
        )
        binding.historiesView.adapter = adapter
        binding.historiesView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean = false
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
        binding.toolbar.inflateMenu(R.menu.view_history)
    }

    private fun finishWithResult(uri: Uri) {
        val intent = Intent()
        intent.setData(uri)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()

        if (adapter.itemCount == 0) {
            Toaster.tShort(this, getString(R.string.message_none_search_histories))
            finish()
            return
        }

        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.clear -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_clear_cache)
                        .setMessage(Html.fromHtml(getString(R.string.confirm_clear_all_settings)))
                        .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                        .setPositiveButton(R.string.ok) { d, i ->
                            adapter.clearAll{ Toaster.snackShort(binding.root, R.string.done_clear, colorPair())}
                            d.dismiss()
                            finish()
                        }
                        .setCancelable(true)
                        .show()
                return true
            }
            R.id.to_top -> {
                RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
                return true
            }
            R.id.to_bottom -> {
                RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
                return true
            }
        }
        return super.clickMenu(item)
    }

    override fun titleId(): Int = R.string.title_view_history

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
    }

    companion object {
        @LayoutRes const val LAYOUT_ID: Int = R.layout.activity_view_history

        /** Request code. */
        const val REQUEST_CODE: Int = 201

        fun makeIntent(context: Context): Intent {
            val intent: Intent = Intent(context, ViewHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}