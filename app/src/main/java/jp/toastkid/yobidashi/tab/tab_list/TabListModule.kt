package jp.toastkid.yobidashi.tab.tab_list

import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.databinding.ModuleTabListBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.TabAdapter

/**
 * WebTab list module.
 *
 * @param binding
 * @param tabAdapter
 * @param parent
 * @param closeAction
 * @param openEditorAction
 * @param openPdfAction
 *
 * @author toastkidjp
 */
class TabListModule(
        private val binding: ModuleTabListBinding,
        private val tabAdapter: TabAdapter,
        private val parent: View,
        private val closeAction: () -> Unit,
        private val openEditorAction: () -> Unit,
        private val openPdfAction: () -> Unit
) : BaseModule(binding.root) {

    /**
     * WebTab list adapter.
     */
    private val adapter: Adapter by lazy { Adapter(context(), tabAdapter, closeAction) }

    /**
     * For showing [android.support.design.widget.Snackbar].
     */
    private val colorPair: ColorPair

    /**
     * For showing [android.support.design.widget.Snackbar].
     */
    private var firstLaunch: Boolean = true

    /**
     * Tab ID when this module opened.
     */
    private var lastTabId: String = ""

    /**
     * Initialize with parent.
     */
    init {
        val preferenceApplier = PreferenceApplier(parent.context)
        colorPair = preferenceApplier.colorPair()

        initRecyclerView(binding.recyclerView)

        initAddEditorTabButton()

        initAddTabButton(binding.addTab)

        initClearTabs(binding.clearTabs)

        binding.addPdfTab.setOnClickListener { openPdfAction() }

        binding.root.setOnClickListener {
            closeAction()
        }
    }

    /**
     * Initialize add-editor-tab button.
     */
    private fun initAddEditorTabButton() =
            binding.addEditorTab.setOnClickListener {
                it.isClickable = false
                openEditorAction()
                closeAction()
                it.isClickable = true
            }

    /**
     * Initialize FAB of "clear tabs".
     *
     * @param clearTabs
     */
    private fun initClearTabs(clearTabs: FloatingActionButton) =
            clearTabs.setOnClickListener { v ->
                val context = v.context
                if (context is FragmentActivity) {
                    val fragmentManager = context.supportFragmentManager
                    val targetFragment =
                            fragmentManager.findFragmentByTag(BrowserFragment::class.java.simpleName)
                    val dialogFragment = TabListClearDialogFragment()
                    dialogFragment.setTargetFragment(targetFragment, 1)
                    dialogFragment.show(
                            fragmentManager,
                            TabListClearDialogFragment::class.java.simpleName
                    )
                }
            }

    /**
     * Initialize recyclerView.
     *
     * @param recyclerView
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
        DragAttachment(recyclerView, ItemTouchHelper.RIGHT)
        DragAttachment(recyclerView, ItemTouchHelper.LEFT)

        recyclerView.adapter = adapter
    }

    /**
     * Initialize adding tab fab.
     *
     * @param addTab fab
     */
    private fun initAddTabButton(addTab: FloatingActionButton) =
            addTab.setOnClickListener { v ->
                addTab.isClickable = false
                tabAdapter.openNewWebTab()
                adapter.notifyItemInserted(adapter.itemCount - 1)
                closeAction()
                addTab.isClickable = true
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

}
