package jp.toastkid.yobidashi.browser.tab

import android.content.res.Resources
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.MenuPos
import jp.toastkid.yobidashi.databinding.ModuleTabListBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Tab list module.

 * @author toastkidjp
 */
class TabListModule
/**
 * Initialize with parent.

 * @param binding
 */
(
        binding: ModuleTabListBinding,
        tabAdapter: TabAdapter
) : BaseModule(binding.root) {

    /** Tab list adapter.  */
    private var adapter: Adapter? = null

    /** For showing snackbar.  */
    private val parent: View

    /** For showing snackbar.  */
    private val colorPair: ColorPair

    /** For showing snackbar.  */
    private var firstLaunch: Boolean = false

    init {
        parent = binding.root

        val preferenceApplier = PreferenceApplier(parent.context)
        colorPair = preferenceApplier.colorPair()

        initRecyclerView(binding.recyclerView, tabAdapter)

        initAddTabButton(binding.addTab, tabAdapter, preferenceApplier.menuPos())
    }

    /**
     * Initialize recyclerView.

     * @param recyclerView
     * *
     * @param tabAdapter
     */
    private fun initRecyclerView(
            recyclerView: RecyclerView,
            tabAdapter: TabAdapter
    ) {
        recyclerView.layoutManager = LinearLayoutManager(context(), LinearLayoutManager.HORIZONTAL, false)
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP, ItemTouchHelper.UP) {
                    override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) {
                        (viewHolder as ViewHolder).close()
                    }
                }).attachToRecyclerView(recyclerView)
        adapter = Adapter(context(), tabAdapter, Runnable { this.hide() })
        recyclerView.adapter = adapter
    }

    /**
     * Initialize adding tab fab.
     * @param addTab fab
     * *
     * @param tabAdapter
     * *
     * @param menuPos
     */
    private fun initAddTabButton(
            addTab: FloatingActionButton,
            tabAdapter: TabAdapter,
            menuPos: MenuPos
    ) {
        addTab.setOnClickListener { v ->
            addTab.isClickable = false
            tabAdapter.openNewTab()
            tabAdapter.setIndex(tabAdapter.size() - 1)
            adapter!!.notifyItemInserted(adapter!!.itemCount - 1)
            hide()
            addTab.isClickable = true
        }

        val resources = context().resources
        val fabMarginHorizontal = resources.getDimensionPixelSize(R.dimen.fab_margin_horizontal)
        MenuPos.place(addTab, 0, fabMarginHorizontal, menuPos)
    }

    override fun show() {
        super.show()
        adapter!!.notifyDataSetChanged()
        if (firstLaunch) {
            Toaster.snackShort(parent, R.string.message_tutorial_remove_tab, colorPair)
            firstLaunch = true
        }
    }
}
