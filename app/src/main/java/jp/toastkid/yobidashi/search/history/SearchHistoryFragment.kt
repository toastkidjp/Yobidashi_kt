package jp.toastkid.yobidashi.search.history

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSearchHistoryBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import jp.toastkid.yobidashi.search.history.usecase.ClearItemsUseCase

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryFragment : Fragment() {

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
                { },
                false
        )
        binding.historiesView.adapter = adapter

        SwipeActionAttachment().invoke(binding.historiesView)

        setHasOptionsMenu(true)

        binding.historiesView.scheduleLayoutAnimation()

        parentFragmentManager.setFragmentResultListener(
            "clear_search_history_items",
            viewLifecycleOwner,
            { _, _ ->
                val showToast: () -> Unit = {
                    Toaster.snackShort(
                        binding.root,
                        R.string.settings_color_delete,
                        preferenceApplier.colorPair()
                    )
                }
                ClearItemsUseCase({ adapter.clearAll(showToast) }).invoke(activity)
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this).get(SearchFragmentViewModel::class.java)
        viewModel.search.observe(viewLifecycleOwner, {
            val event = it.getContentIfNotHandled() ?: return@observe
            SearchAction(view.context, event.category ?: "", event.query).invoke()
        })
        adapter.setViewModel(viewModel)
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
        return when (item.itemId) {
            R.id.clear -> {
                ConfirmDialogFragment.show(
                    parentFragmentManager,
                    getString(R.string.title_clear_search_history),
                    Html.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    ),
                    "clear_search_history_items"
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        parentFragmentManager.clearFragmentResultListener("clear_search_history_items")
        super.onDetach()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_search_history

    }
}