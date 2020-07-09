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
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSearchHistoryBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryFragment : Fragment(),
        SearchHistoryClearDialogFragment.OnClickSearchHistoryClearCallback {

    private lateinit var binding: FragmentSearchHistoryBinding

    private lateinit var adapter: ModuleAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.historiesView.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        adapter = ModuleAdapter(
                context,
                DatabaseFinder().invoke(context).searchHistoryRepository(),
                { SearchAction(context, it.category ?: "", it.query ?: "").invoke()},
                { },
                { },
                false
        )
        binding.historiesView.adapter = adapter

        SwipeActionAttachment().invoke(binding.historiesView)

        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                val fragmentManager = fragmentManager ?: return true
                SearchHistoryClearDialogFragment.show(
                        fragmentManager,
                        this
                )
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onClickSearchHistoryClear() {
        val context = context ?: return
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                DatabaseFinder().invoke(context).searchHistoryRepository().deleteAll()
            }

            adapter.clearAll {
                Toaster.snackShort(binding.root, R.string.settings_color_delete, preferenceApplier.colorPair())
            }
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_search_history

    }
}