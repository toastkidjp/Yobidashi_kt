package jp.toastkid.yobidashi.tab.tab_list

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogFragmentTabListBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.image.BackgroundImageLoaderUseCase
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.tab.model.Tab

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        val preferenceApplier = PreferenceApplier(activityContext)
        colorPair = preferenceApplier.colorPair()

        val target = activity
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        if (target is Callback) {
            callback = target
        } else {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        initializeContentView(activityContext)

        BackgroundImageLoaderUseCase().invoke(
                binding.background,
                PreferenceApplier(activityContext).backgroundImagePath
        )

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.TabListBottomSheetDialog)
        return super.onCreateDialog(savedInstanceState)
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

        colorPair.applyTo(binding.addArticleTab)
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

    fun openArticleList() {
        val activity = activity ?: return
        ViewModelProvider(activity).get(ContentViewModel::class.java).openArticleList()
        callback?.onCloseOnly()
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
        if (context is MainActivity) {
            ConfirmDialogFragment.show(
                parentFragmentManager,
                getString(R.string.title_clear_all_tabs),
                Html.fromHtml(
                    getString(R.string.confirm_clear_all_settings),
                    Html.FROM_HTML_MODE_COMPACT
                ),
                "clear_tabs"
            )
        }
    }

    /**
     * Initialize recyclerView.
     *
     * @param recyclerView
     */
    private fun initRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelperAttachment()(recyclerView)

        LinearSnapHelper().attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_fragment_tab_list
    }
}
