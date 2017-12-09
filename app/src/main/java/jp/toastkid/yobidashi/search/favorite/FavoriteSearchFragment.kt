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
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentFavoriteSearchBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.Clear
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * Favorite search fragment.
 *
 * @author toastkidjp
 */
class FavoriteSearchFragment : BaseFragment() {

    /** RecyclerView's adapter  */
    private var adapter: ActivityAdapter? = null

    /** Data Binding object.  */
    private var binding: FragmentFavoriteSearchBinding? = null

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentFavoriteSearchBinding>(
                inflater!!, LAYOUT_ID, container, false)
        binding!!.activity = this

        initFavSearchsView()

        setHasOptionsMenu(true)

        return binding!!.root
    }

    private fun initFavSearchsView() {
        adapter = ActivityAdapter(
                activity,
                DbInitter.init(activity).relationOfFavoriteSearch(),
                { category, query -> this.startSearch(category, query) },
                { messageId -> Toaster.snackShort(binding!!.content, messageId, colorPair()) }
        )
        binding!!.favoriteSearchView.adapter = adapter
        binding!!.favoriteSearchView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
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
        SearchAction(activity, category.name, query).invoke().addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.favorite_toolbar_menu, menu)

        menu!!.findItem(R.id.favorite_toolbar_menu_clear).setOnMenuItemClickListener { v ->
            Clear(binding!!.favoriteSearchView, adapter!!.relation.deleter()).invoke()
            true
        }

        menu.findItem(R.id.favorite_toolbar_menu_add).setOnMenuItemClickListener { v ->
            invokeAddition()
            true
        }
    }

    fun add(v: View) {
        invokeAddition()
    }

    private fun invokeAddition() {
        Addition(
                binding!!.additionArea,
                { messageId -> Toaster.snackShort(binding!!.content, messageId, colorPair()) }
        ).invoke()
    }

    @StringRes
    override fun titleId(): Int {
        return R.string.title_favorite_search
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.fragment_favorite_search
    }
}