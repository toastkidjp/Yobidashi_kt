package jp.toastkid.yobidashi.search.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySearchHistoryBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryActivity : Fragment(),
        SearchHistoryClearDialogFragment.OnClickSearchHistoryClearCallback {

    private lateinit var binding: ActivitySearchHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.historiesView.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        // TODO move it.
        val repository = DatabaseFinder().invoke(context).searchHistoryRepository()

        adapter = ActivityAdapter(
                context,
                repository,
                { history -> SearchAction(context, history.category as String, history.query as String).invoke()},
                { history -> Toaster.snackShort(binding.root, history.query as String, preferenceApplier.colorPair()) }
        )
        binding.historiesView.adapter = adapter

        SwipeActionAttachment().invoke(binding.historiesView)

        binding.historiesView.scheduleLayoutAnimation()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        adapter.refresh {
            Toaster.tShort(binding.root.context, getString(R.string.message_none_search_histories))
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.search_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.clear -> {
                SearchHistoryClearDialogFragment().show(
                        fragmentManager,
                        SearchHistoryClearDialogFragment::class.java.simpleName
                )
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onClickSearchHistoryClear() {
        adapter.clearAll { Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair()) }
                .addTo(disposables)
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_search_history

    }
}