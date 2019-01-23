package jp.toastkid.yobidashi.tab.tab_list

import android.app.Dialog
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.databinding.DialogFragmentTabListBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.model.Tab

/**
 * Tab list dialog fragment.
 *
 * @author toastkidjp
 */
class TabListDialogFragment : DialogFragment() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: DialogFragmentTabListBinding

    /**
     * WebTab list adapter.
     */
    private lateinit var adapter: Adapter

    /**
     * For showing [android.support.design.widget.Snackbar].
     */
    private lateinit var colorPair: ColorPair

    /**
     * Callback of tab-list adapter and BrowserFragment.
     */
    private var callback: TabListDialogFragment.Callback? = null

    /**
     * For showing [android.support.design.widget.Snackbar].
     */
    private var firstLaunch: Boolean = true

    /**
     * Tab ID when this module opened.
     */
    private var lastTabId: String = ""

    interface Callback {
        fun onCloseTabListDialogFragment()
        fun onOpenEditor()
        fun onOpenPdf()
        fun openNewTabFromTabList()
        fun tabIndexFromTabList(): Int
        fun currentTabIdFromTabList(): String
        fun replaceTabFromTabList(tab: Tab)
        fun getTabByIndexFromTabList(position: Int): Tab
        fun closeTabFromTabList(position: Int)
        fun getTabAdapterSizeFromTabList(): Int
        fun swapTabsFromTabList(from: Int, to: Int)
        fun tabIndexOfFromTabList(tab: Tab): Int
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context
                ?: return super.onCreateDialog(savedInstanceState)


        val preferenceApplier = PreferenceApplier(activityContext)
        colorPair = preferenceApplier.colorPair()

        val target = targetFragment ?: return super.onCreateDialog(savedInstanceState)
        if (target is TabListDialogFragment.Callback) {
            callback = target
        } else {
            return super.onCreateDialog(savedInstanceState)
        }

        initializeContentView(activityContext)

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialogStyle)

        return AlertDialog.Builder(activityContext)
                .setView(binding.root)
                .create()
                .also {
                    it.window?.also { window ->
                        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                }
    }

    /**
     * Initialize views.
     *
     * @param activityContext [Context]
     */
    private fun initializeContentView(activityContext: Context) {
        val index = callback?.tabIndexFromTabList() ?: 0

        adapter = Adapter(activityContext, callback as Callback)
        adapter.setCurrentIndex(index)
        adapter.notifyDataSetChanged()

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(activityContext),
                R.layout.dialog_fragment_tab_list, null, false)

        initRecyclerView(binding.recyclerView)

        initAddEditorTabButton()

        initAddTabButton(binding.addTab)

        initClearTabs(binding.clearTabs)

        binding.addPdfTab.setOnClickListener { callback?.onOpenPdf() }

        binding.recyclerView.layoutManager.scrollToPosition(index)
        binding.recyclerView.scheduleLayoutAnimation()
        if (firstLaunch) {
            Toaster.snackShort(
                    binding.recyclerView,
                    R.string.message_tutorial_remove_tab,
                    colorPair
            )
            firstLaunch = false
        }
        lastTabId = callback?.currentTabIdFromTabList() ?: ""
    }

    /**
     * Initialize add-editor-tab button.
     */
    private fun initAddEditorTabButton() =
            binding.addEditorTab.setOnClickListener {
                it.isClickable = false
                callback?.onOpenEditor()
                callback?.onCloseTabListDialogFragment()
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
        recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
            addTab.setOnClickListener {
                addTab.isClickable = false
                callback?.openNewTabFromTabList()
                adapter.notifyItemInserted(adapter.itemCount - 1)
                callback?.onCloseTabListDialogFragment()
                addTab.isClickable = true
            }

    companion object {

        /**
         * Make this DialogFragment instance.
         *
         * @param target target [Fragment]
         */
        fun make(target: Fragment): TabListDialogFragment {
            val tabListDialogFragment = TabListDialogFragment()
            tabListDialogFragment.setTargetFragment(target, 1)
            return tabListDialogFragment
        }
    }
}
