package jp.toastkid.yobidashi.tab.tab_list

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.databinding.DialogFragmentTabListBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.model.Tab
import java.io.File

/**
 * Tab list dialog fragment.
 *
 * @author toastkidjp
 */
class TabListDialogFragment : BottomSheetDialogFragment() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: DialogFragmentTabListBinding

    /**
     * WebTab list adapter.
     */
    private lateinit var adapter: Adapter

    /**
     * For showing [com.google.android.material.snackbar.Snackbar].
     */
    private lateinit var colorPair: ColorPair

    /**
     * Callback of tab-list adapter and BrowserFragment.
     */
    private var callback: Callback? = null

    /**
     * For showing [com.google.android.material.snackbar.Snackbar].
     */
    private var firstLaunch: Boolean = true

    /**
     * Tab ID when this module opened.
     */
    private var lastTabId: String = ""

    private var rightTouchHelper: ItemTouchHelper? = null

    private var leftTouchHelper: ItemTouchHelper? = null

    interface Callback {
        fun onCloseOnly()
        fun onCloseTabListDialogFragment()
        fun onOpenEditor()
        fun onOpenPdf()
        fun openNewTabFromTabList()
        fun tabIndexFromTabList(): Int
        fun currentTabIdFromTabList(): String
        fun replaceTabFromTabList(tab: Tab)
        fun getTabByIndexFromTabList(position: Int): Tab?
        fun closeTabFromTabList(position: Int)
        fun getTabAdapterSizeFromTabList(): Int
        fun swapTabsFromTabList(from: Int, to: Int)
        fun tabIndexOfFromTabList(tab: Tab): Int
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        val preferenceApplier = PreferenceApplier(activityContext)
        colorPair = preferenceApplier.colorPair()

        val target = activity ?: return super.onCreateView(inflater, container, savedInstanceState)
        if (target is Callback) {
            callback = target
        } else {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        initializeContentView(activityContext)
        applyBackgrounds()
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback?.onCloseTabListDialogFragment()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        callback?.onCloseTabListDialogFragment()
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

        // TODO use data binding
        binding.addPdfTab.setOnClickListener { callback?.onOpenPdf() }

        colorPair.applyTo(binding.addPdfTab)
        colorPair.applyTo(binding.addEditorTab)
        colorPair.applyTo(binding.addTab)
        colorPair.applyTo(binding.clearTabs)

        binding.recyclerView.layoutManager?.scrollToPosition(index)
        binding.recyclerView.scheduleLayoutAnimation()
        if (firstLaunch) {
            Toaster.snackShort(
                    binding.snackbarParent,
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
                it.isClickable = true
                dismiss()
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
                            fragmentManager.findFragmentByTag(BrowserFragment::class.java.canonicalName)
                    val dialogFragment = TabListClearDialogFragment()
                    dialogFragment.setTargetFragment(targetFragment, 1)
                    dialogFragment.show(
                            fragmentManager,
                            TabListClearDialogFragment::class.java.canonicalName
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

        val dragAttachment = DragAttachment()
        rightTouchHelper = dragAttachment(recyclerView, ItemTouchHelper.RIGHT)
        leftTouchHelper = dragAttachment(recyclerView, ItemTouchHelper.LEFT)

        LinearSnapHelper().attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter

        val activity = requireActivity()
        ViewModelProviders.of(activity)
                .get(TabListViewModel::class.java)
                .startDrag
                .observe(activity, Observer { startDrag(it) })
    }

    private fun startDrag(viewHolder: RecyclerView.ViewHolder) {
        rightTouchHelper?.startDrag(viewHolder)
        leftTouchHelper?.startDrag(viewHolder)
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
                addTab.isClickable = true
                dismiss()
            }

    // TODO extract class
    private fun applyBackgrounds() {
        val backgroundImagePath = PreferenceApplier(requireContext()).backgroundImagePath
        if (backgroundImagePath.isEmpty()) {
            return
        }

        Glide.with(this)
                .load(File(backgroundImagePath).toURI().toString().toUri())
                .override(binding.root.measuredWidth, binding.root.measuredHeight)
                .into(binding.background)
    }

    companion object {

        /**
         * Make this DialogFragment instance.
         * TODO delete it.
         * @param target target [Fragment]
         */
        fun make(target: Fragment): TabListDialogFragment {
            val tabListDialogFragment = TabListDialogFragment()
            return tabListDialogFragment
        }
    }
}
