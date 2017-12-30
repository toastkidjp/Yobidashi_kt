package jp.toastkid.yobidashi.tab.tab_list

import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.text.TextUtils
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.MenuPos
import jp.toastkid.yobidashi.databinding.ModuleTabListBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.tab.TabAdapter

/**
 * WebTab list module.
 *
 * @author toastkidjp
 */
class TabListModule(
        private val binding: ModuleTabListBinding,
        private val tabAdapter: TabAdapter,
        private val parent: View,
        private val closeAction: () -> Unit,
        private val openPdfAction: () -> Unit,
        private val emptyAction: () -> Unit
) : BaseModule(binding.root) {

    /** WebTab list adapter.  */
    private val adapter: Adapter by lazy { Adapter(context(), tabAdapter, closeAction) }

    /** For showing snackbar.  */
    private val colorPair: ColorPair

    /** For showing snackbar.  */
    private var firstLaunch: Boolean = true

    /**
     * Tab ID when this module opened.
     */
    private var lastTabId: String = ""

    /**
     * Initialize with parent.
     *
     * @param binding
     */
    init {
        val preferenceApplier = PreferenceApplier(parent.context)
        colorPair = preferenceApplier.colorPair()

        initRecyclerView(binding.recyclerView)

        initAddEditorTabButton()

        initAddTabButton(binding.addTab)

        initClearTabs(binding.clearTabs)

        binding.addPdfTab.setOnClickListener { openPdfAction() }

        val context = context()

        binding.addSearchTab.setOnClickListener {
            context.startActivity(SearchActivity.makeIntent(context))
        }

        val menuPos = preferenceApplier.menuPos()
        val resources = context.resources
        val fabMarginHorizontal = resources.getDimensionPixelSize(R.dimen.fab_margin_horizontal)
        MenuPos.place(binding.fabs, 0, fabMarginHorizontal, menuPos)
    }

    /**
     * Initialize add-editor-tab button.
     */
    private fun initAddEditorTabButton() {
        binding.addEditorTab.setOnClickListener {
            it.isClickable = false
            tabAdapter.openNewEditorTab()
            adapter.notifyItemInserted((adapter.itemCount ?: 1) - 1)
            closeAction()
            it.isClickable = true
        }
    }

    private fun initClearTabs(clearTabs: FloatingActionButton) {
        clearTabs.setOnClickListener { v ->
            AlertDialog.Builder(context())
                    .setTitle(context().getString(R.string.title_clear_all_tabs))
                    .setMessage(Html.fromHtml(context().getString(R.string.confirm_clear_all_settings)))
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                    .setPositiveButton(R.string.ok) { d, i ->
                        tabAdapter.clear()
                        emptyAction()
                        d.dismiss()
                    }
                    .show()
        }
    }

    /**
     * Initialize recyclerView.

     * @param recyclerView
     *
     * @param tabAdapter
     */
    private fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context(), LinearLayoutManager.HORIZONTAL, false)
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP, ItemTouchHelper.UP) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean = true

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) = (viewHolder as ViewHolder).close()
                }).attachToRecyclerView(recyclerView)
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
    private fun initAddTabButton(addTab: FloatingActionButton) {
        addTab.setOnClickListener { v ->
            addTab.isClickable = false
            tabAdapter.openNewTab()
            adapter.notifyItemInserted(adapter.itemCount - 1)
            closeAction()
            addTab.isClickable = true
        }
    }

    override fun show() {
        binding.recyclerView.layoutManager.scrollToPosition(tabAdapter.index())
        adapter.setCurrentIndex(tabAdapter.index())
        adapter.notifyDataSetChanged()
        binding.recyclerView.scheduleLayoutAnimation()
        super.show()
        if (firstLaunch) {
            Toaster.snackShort(parent, R.string.message_tutorial_remove_tab, colorPair)
            firstLaunch = false
        }
        lastTabId = tabAdapter.currentTabId()
    }

    override fun hide() {
        super.hide()

        if (tabAdapter.isEmpty()) {
            return
        }

        if (!TextUtils.equals(lastTabId, tabAdapter.currentTabId())) {
            tabAdapter.reloadUrlIfNeed()
        }
    }

}
