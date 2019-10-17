package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySearchHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier
import jp.toastkid.yobidashi.search.SearchAction

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryActivity : AppCompatActivity(),
        SearchHistoryClearDialogFragment.OnClickSearchHistoryClearCallback {

    private lateinit var binding: ActivitySearchHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        binding.historiesView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val repository = DatabaseFinder().invoke(this).searchHistoryRepository()

        adapter = ActivityAdapter(
                this,
                repository,
                { history -> SearchAction(this, history.category as String, history.query as String).invoke()},
                { history -> Toaster.snackShort(binding.root, history.query as String, preferenceApplier.colorPair()) }
        )
        binding.historiesView.adapter = adapter

        SwipeActionAttachment.invoke(binding.historiesView)

        binding.toolbar.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(titleId())
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.inflateMenu(R.menu.search_history)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }

        binding.historiesView.scheduleLayoutAnimation()
    }

    override fun onResume() {
        super.onResume()

        adapter.refresh()

        ToolbarColorApplier()(window, binding.toolbar, preferenceApplier.colorPair())

        ImageLoader.setImageToImageView(binding.background, preferenceApplier.backgroundImagePath)
    }

    override fun onPostResume() {
        super.onPostResume()

        if (adapter.itemCount == 0) {
            Toaster.tShort(this, getString(R.string.message_none_search_histories))
            finish()
        }
    }

    private fun clickMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> finish()
            R.id.clear -> SearchHistoryClearDialogFragment().show(
                    supportFragmentManager,
                    SearchHistoryClearDialogFragment::class.java.simpleName
            )
            R.id.menu_exit -> moveTaskToBack(true)
        }
        return true
    }

    override fun onClickSearchHistoryClear() {
        adapter.clearAll { Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair()) }
                .addTo(disposables)
        finish()
    }

    private fun titleId(): Int = R.string.title_search_history

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_search_history

        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, SearchHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}