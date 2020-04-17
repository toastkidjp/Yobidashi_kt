package jp.toastkid.yobidashi.browser.history

import android.net.Uri
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentViewHistoryBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.main.content.ContentViewModel

/**
 * @author toastkidjp
 */
class ViewHistoryFragment: Fragment(), ClearDialogFragment.Callback, ContentScrollable {

    private lateinit var binding: FragmentViewHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = requireContext()
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.historiesView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        contentViewModel = ViewModelProviders.of(requireActivity()).get(ContentViewModel::class.java)

        adapter = ActivityAdapter(
                context,
                DatabaseFinder().invoke(context).viewHistoryRepository(),
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> contentViewModel?.snackShort(history.title) }
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

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentActivity = activity ?: return
        ViewModelProviders.of(fragmentActivity).get(PageSearcherViewModel::class.java)
                .find
                .observe(fragmentActivity, Observer {
                    adapter.filter(it)
                })
    }

    private fun finishWithResult(uri: Uri?) {
        if (uri == null) {
            return
        }

        val browserViewModel =
                ViewModelProviders.of(requireActivity()).get(BrowserViewModel::class.java)

        popBackStack()
        browserViewModel.open(uri)
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onResume() {
        super.onResume()

        adapter.refresh {
            if (adapter.itemCount != 0) {
                return@refresh
            }

            contentViewModel?.snackShort(R.string.message_none_search_histories)
            popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.view_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.clear -> {
                val clearDialogFragment = ClearDialogFragment()
                clearDialogFragment.setTargetFragment(this, clearDialogFragment.id)
                clearDialogFragment.show(
                        fragmentManager,
                        ClearDialogFragment::class.java.simpleName
                )
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onClickClear() {
        adapter.clearAll{ contentViewModel?.snackShort(R.string.done_clear)}
        popBackStack()
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
    }

    override fun onDetach() {
        super.onDetach()
        adapter.dispose()
    }

    companion object {
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_view_history

    }
}