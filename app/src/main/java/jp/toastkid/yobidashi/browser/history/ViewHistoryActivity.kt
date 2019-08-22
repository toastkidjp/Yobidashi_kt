package jp.toastkid.yobidashi.browser.history

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityViewHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitializer
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier

/**
 * @author toastkidjp
 */
class ViewHistoryActivity: AppCompatActivity(), ClearDialogFragment.Callback {

    private lateinit var binding: ActivityViewHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView<ActivityViewHistoryBinding>(this, LAYOUT_ID)
        val relation = DbInitializer.init(this).relationOfViewHistory()

        binding.historiesView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> Toaster.snackShort(binding.root, history.title, preferenceApplier.colorPair()) }
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

        binding.toolbar.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(titleId())
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.inflateMenu(R.menu.view_history)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }
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

        ToolbarColorApplier()(window, binding.toolbar, preferenceApplier.colorPair())

        ImageLoader.setImageToImageView(binding.background, preferenceApplier.backgroundImagePath)
    }

    private fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.clear -> {
                ClearDialogFragment().show(
                        supportFragmentManager,
                        ClearDialogFragment::class.java.simpleName
                )
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
            R.id.menu_exit -> {
                moveTaskToBack(true)
                return true
            }
            R.id.menu_close -> {
                finish()
                return true
            }
        }
        return true
    }

    override fun onClickClear() {
        adapter.clearAll{ Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair())}
        finish()
    }

    private fun titleId(): Int = R.string.title_view_history

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
    }

    companion object {
        @LayoutRes const val LAYOUT_ID: Int = R.layout.activity_view_history

        /** Request code. */
        const val REQUEST_CODE: Int = 201

        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, ViewHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}