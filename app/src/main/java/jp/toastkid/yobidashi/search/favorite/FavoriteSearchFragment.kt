package jp.toastkid.yobidashi.search.favorite

import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentFavoriteSearchBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitializer
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * Favorite search fragment.
 *
 * @author toastkidjp
 */
class FavoriteSearchFragment : Fragment(),
        CommonFragmentAction,
        ClearFavoriteSearchDialogFragment.Callback {

    /**
     * RecyclerView's adapter
     */
    private var adapter: ActivityAdapter? = null

    /**
     * Data Binding object.
     */
    private var binding: FragmentFavoriteSearchBinding? = null

    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * [CompositeDisposable].
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentFavoriteSearchBinding>(
                inflater, LAYOUT_ID, container, false)
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
        adapter = ActivityAdapter(
                fragmentActivity,
                DbInitializer.init(fragmentActivity).relationOfFavoriteSearch(),
                { category, query -> this.startSearch(category, query) },
                { messageId -> Toaster.snackShort(binding?.content as View, messageId, colorPair()) }
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

    /**
     * Start search action.
     *
     * @param category Search category
     * @param query    Search query
     */
    private fun startSearch(category: SearchCategory, query: String) {
        activity?.let {
            SearchAction(it, category.name, query).invoke().addTo(disposables)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        // TODO Clean up
        inflater?.inflate(R.menu.favorite_toolbar_menu, menu)

        menu?.findItem(R.id.favorite_toolbar_menu_clear)?.setOnMenuItemClickListener {
            val fragmentManager = fragmentManager ?: return@setOnMenuItemClickListener true
            ClearFavoriteSearchDialogFragment.show(
                    fragmentManager,
                    this::class.java
            )
            true
        }

        menu?.findItem(R.id.favorite_toolbar_menu_add)?.setOnMenuItemClickListener {
            invokeAddition()
            true
        }
    }

    override fun onClickDeleteAllFavoriteSearch() {
        val activityContext = context ?: return

        Completable.fromAction { adapter?.relation?.deleter()?.execute() }
                ?.subscribeOn(Schedulers.io())
                ?.subscribe {
                    Toaster.snackShort(
                            binding?.root as View,
                            R.string.settings_color_delete,
                            colorPair()
                    )
                }
                ?.addTo(disposables)
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
        val layout = binding?.additionArea ?: return
        Addition(layout) { messageId -> Toaster.snackShort(layout, messageId, colorPair()) }.invoke()
    }

    private fun colorPair() = preferenceApplier.colorPair()

    @StringRes
    override fun titleId() = R.string.title_favorite_search

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    companion object {

        /**
         * Layout ID.
         */
        private const val LAYOUT_ID = R.layout.fragment_favorite_search
    }
}