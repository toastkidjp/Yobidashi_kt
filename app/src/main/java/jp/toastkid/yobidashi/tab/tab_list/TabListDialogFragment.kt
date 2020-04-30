package jp.toastkid.yobidashi.tab.tab_list

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
        fun onCloseTabListDialogFragment(lastTabId: String)
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
        callback?.onCloseTabListDialogFragment(lastTabId)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        callback?.onCloseTabListDialogFragment(lastTabId)
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
                LAYOUT_ID,
                null,
                false
        )

        binding.dialog = this

        initRecyclerView(binding.recyclerView)

        colorPair.applyTo(binding.addPdfTab)
        colorPair.applyTo(binding.addEditorTab)
        colorPair.applyTo(binding.addTab)
        colorPair.applyTo(binding.clearTabs)

        binding.recyclerView.layoutManager?.scrollToPosition(index - 1)
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

    fun openPdf() {
        callback?.onOpenPdf()
    }

    fun openEditor(view: View) {
        view.isClickable = false
        callback?.onOpenEditor()
        view.isClickable = true
        dismiss()
    }

    fun addTab(view: View) {
        view.isClickable = false
        callback?.openNewTabFromTabList()
        adapter.notifyItemInserted(adapter.itemCount - 1)
        view.isClickable = true
        dismiss()
    }

    fun clearTabs(v: View) {
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
                    ) = (viewHolder as? ViewHolder)?.close() ?: Unit
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

        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_fragment_tab_list
    }
}
