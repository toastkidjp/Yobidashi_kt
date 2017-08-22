package jp.toastkid.jitte.browser.tab

import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import jp.toastkid.jitte.R
import jp.toastkid.jitte.browser.MenuPos
import jp.toastkid.jitte.databinding.ModuleTabListBinding
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.facade.BaseModule
import jp.toastkid.jitte.libs.preference.ColorPair
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * Tab list module.
 *
 * @author toastkidjp
 */
class TabListModule(
        val binding: ModuleTabListBinding,
        val tabAdapter: TabAdapter,
        private val parent: View,
        private val closeAction: () -> Unit
) : BaseModule(binding.root) {

    /** Tab list adapter.  */
    private var adapter: Adapter? = null

    /** For showing snackbar.  */
    private val colorPair: ColorPair

    /** For showing snackbar.  */
    private var firstLaunch: Boolean = true

    /**
     * Initialize with parent.
     *
     * @param binding
     */
    init {
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
                            rv: RecyclerView,
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
        adapter = Adapter(context(), tabAdapter, closeAction)
        recyclerView.adapter = adapter
    }

    /**
     * Initialize adding tab fab.
     * @param addTab fab
     *
     * @param tabAdapter
     *
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
            closeAction()
            addTab.isClickable = true
        }

        val resources = context().resources
        val fabMarginHorizontal = resources.getDimensionPixelSize(R.dimen.fab_margin_horizontal)
        MenuPos.place(addTab, 0, fabMarginHorizontal, menuPos)
    }

    override fun show() {
        binding.recyclerView.layoutManager.scrollToPosition(tabAdapter.index())
        adapter?.setCurrentIndex(tabAdapter.index())
        adapter?.notifyDataSetChanged()
        super.show()
        if (firstLaunch) {
            Toaster.snackShort(parent, R.string.message_tutorial_remove_tab, colorPair)
            firstLaunch = false
        }
    }
}
