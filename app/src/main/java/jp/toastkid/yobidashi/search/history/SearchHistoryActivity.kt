package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.MenuItem
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySearchHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.search.SearchAction

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
        val relation = DbInitter.init(this).relationOfSearchHistory()

        binding.historiesView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> SearchAction(this, history.category as String, history.query as String).invoke()},
                { history -> Toaster.snackShort(binding.root, history.query as String, colorPair()) }
        )
        binding.historiesView.adapter = adapter

        SwipeActionAttachment.invoke(binding.historiesView)

        initToolbar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.search_history)
        binding.historiesView.scheduleLayoutAnimation()
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
            val intent = Intent(context, SearchHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}