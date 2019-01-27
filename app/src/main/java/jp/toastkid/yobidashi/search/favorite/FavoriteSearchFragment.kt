package jp.toastkid.yobidashi.search.favorite

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseFragment
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
class FavoriteSearchFragment : BaseFragment(), ClearFavoriteSearchDialogFragment.Callback {

    /** RecyclerView's adapter  */
    private var adapter: ActivityAdapter? = null

    /** Data Binding object.  */
    private var binding: FragmentFavoriteSearchBinding? = null

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentFavoriteSearchBinding>(
                inflater, LAYOUT_ID, container, false)
        binding!!.activity = this

        initFavSearchView()

        setHasOptionsMenu(true)

        return binding!!.root
    }

    private fun initFavSearchView() {
        val fragmentActivity = activity ?: return
        adapter = ActivityAdapter(
                fragmentActivity,
                DbInitializer.init(fragmentActivity).relationOfFavoriteSearch(),
                { category, query -> this.startSearch(category, query) },
                { messageId -> Toaster.snackShort(binding!!.content, messageId, colorPair()) }
        )
        binding!!.favoriteSearchView.adapter = adapter
        binding!!.favoriteSearchView.layoutManager = LinearLayoutManager(fragmentActivity, LinearLayoutManager.VERTICAL, false)
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        val fromPos = viewHolder.adapterPosition
                        val toPos = target.adapterPosition
                        adapter!!.notifyItemMoved(fromPos, toPos)
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        if (direction != ItemTouchHelper.RIGHT) {
                            return
                        }
                        adapter!!.removeAt(viewHolder.adapterPosition)
                    }
                }).attachToRecyclerView(binding!!.favoriteSearchView)
    }

    /**
     * Start search action.
     * @param category Search category
     *
     * @param query    Search query
     */
    private fun startSearch(category: SearchCategory, query: String) {
        activity?.let {
            SearchAction(it, category.name, query).invoke().addTo(disposables)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.favorite_toolbar_menu, menu)

        menu!!.findItem(R.id.favorite_toolbar_menu_clear).setOnMenuItemClickListener {
            val fragmentManager = fragmentManager ?: return@setOnMenuItemClickListener true
            ClearFavoriteSearchDialogFragment.show(
                    fragmentManager,
                    this::class.java
            )
            true
        }

        menu.findItem(R.id.favorite_toolbar_menu_add).setOnMenuItemClickListener {
            invokeAddition()
            true
        }
    }

    override fun onClickDeleteAllFavoriteSearch() {
        val activityContext = context ?: return

        adapter?.relation?.deleter()?.executeAsSingle()
                ?.subscribeOn(Schedulers.io())
                ?.subscribe { _ ->
                    Toaster.snackShort(
                            binding?.root as View,
                            R.string.settings_color_delete,
                            PreferenceApplier(activityContext).colorPair()
                    )
                }
                ?.addTo(disposables)
    }

    fun add() {
        invokeAddition()
    }

    private fun invokeAddition() {
        Addition(
                binding!!.additionArea,
                { messageId -> Toaster.snackShort(binding!!.content, messageId, colorPair()) }
        ).invoke()
    }

    @StringRes
    override fun titleId() = R.string.title_favorite_search

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    companion object {

        /** Layout ID.  */
        private const val LAYOUT_ID = R.layout.fragment_favorite_search
    }
}