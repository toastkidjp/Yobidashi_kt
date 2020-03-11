package jp.toastkid.yobidashi.browser.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.databinding.ActivityViewHistoryBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * TODO: Implement content scrollable, remove background.
 * @author toastkidjp
 */
class ViewHistoryActivity: Fragment(), ClearDialogFragment.Callback {

    private lateinit var binding: ActivityViewHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = requireContext()
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate<ActivityViewHistoryBinding>(inflater, LAYOUT_ID, container, false)
        val viewHistoryRepository = DatabaseFinder().invoke(context).viewHistoryRepository()

        binding.historiesView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        adapter = ActivityAdapter(
                context,
                viewHistoryRepository,
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
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        adapter.refresh {
            if (adapter.itemCount != 0) {
                return@refresh
            }

            Toaster.tShort(requireContext(), R.string.message_none_search_histories)
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
                clearDialogFragment.setTargetFragment(this, REQUEST_CODE)
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
        adapter.clearAll{ Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair())}
        popBackStack()
    }

    override fun onDetach() {
        super.onDetach()
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