package jp.toastkid.yobidashi.search.favorite

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentFavoriteSearchBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Favorite search fragment.
 *
 * @author toastkidjp
 */
class FavoriteSearchFragment : Fragment(), CommonFragmentAction {

    /**
     * RecyclerView's adapter
     */
    private var adapter: ModuleAdapter? = null

    /**
     * Data Binding object.
     */
    private var binding: FragmentFavoriteSearchBinding? = null

    private lateinit var preferenceApplier: PreferenceApplier

    private val disposables: Job by lazy { Job() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding?.activity = this

        val context = context ?: return binding?.root
        preferenceApplier = PreferenceApplier(context)

        initFavSearchView()

        setHasOptionsMenu(true)

        return binding?.root
    }

    /**
     * Initialize favorite search view.
     */
    private fun initFavSearchView() {
        val fragmentActivity = activity ?: return

        val repository = DatabaseFinder().invoke(fragmentActivity).favoriteSearchRepository()

        adapter = ModuleAdapter(
                fragmentActivity,
                repository,
                { startSearch(jp.toastkid.search.SearchCategory.findByCategory(it.category), it.query ?: "") },
                { },
                { }
        )

        binding?.favoriteSearchView?.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(fragmentActivity, RecyclerView.VERTICAL, false)
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
                        adapter?.notifyItemMoved(fromPos, toPos)
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        if (direction != ItemTouchHelper.RIGHT) {
                            return
                        }
                        adapter?.removeAt(viewHolder.adapterPosition)
                    }
                }).attachToRecyclerView(binding?.favoriteSearchView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel =
                ViewModelProvider(this).get(FavoriteSearchFragmentViewModel::class.java)
        viewModel.reload.observe(viewLifecycleOwner, Observer { adapter?.refresh() })
        viewModel.clear.observe(viewLifecycleOwner, Observer { clear() })
    }

    /**
     * Start search action.
     *
     * @param category Search category
     * @param query    Search query
     */
    private fun startSearch(category: jp.toastkid.search.SearchCategory, query: String) {
        activity?.let {
            SearchAction(it, category.name, query).invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter?.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.favorite_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.favorite_toolbar_menu_clear -> {
                    ClearFavoriteSearchDialogFragment.show(parentFragmentManager, this)
                    true
                }
                R.id.favorite_toolbar_menu_add -> {
                    invokeAddition()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun clear() {
        val context = requireContext()
        val repository = DatabaseFinder().invoke(context).favoriteSearchRepository()

        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) {
                repository.deleteAll()
                adapter?.clear()
            }

            Toaster.snackShort(
                    binding?.root as View,
                    R.string.settings_color_delete,
                    colorPair()
            )
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    /**
     * Implement for called from Data-Binding.
     */
    fun add() {
        invokeAddition()
    }

    /**
     * Invoke addition.
     */
    private fun invokeAddition() {
        FavoriteSearchAdditionDialogFragment()
                .also { it.setTargetFragment(this, 0) }
                .show(parentFragmentManager, "addition")
    }

    private fun colorPair() = preferenceApplier.colorPair()

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_favorite_search
    }
}