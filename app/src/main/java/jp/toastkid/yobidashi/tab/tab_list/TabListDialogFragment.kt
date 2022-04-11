package jp.toastkid.yobidashi.tab.tab_list

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.tab.TabThumbnails
import jp.toastkid.yobidashi.tab.model.Tab
import kotlin.math.max

/**
 * Tab list dialog fragment.
 *
 * @author toastkidjp
 */
class TabListDialogFragment : BottomSheetDialogFragment() {

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

        val composeView = ComposeView(activityContext)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent { TabListUi() }

        return composeView
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun TabListUi() {
        val tabThumbnails = TabThumbnails.with(LocalContext.current)
        val index = callback?.tabIndexFromTabList() ?: 0
        val state = rememberLazyListState(max(0, index - 1))
        val rememberCoroutineScope = rememberCoroutineScope()

        val tabs = remember { mutableStateListOf<Tab>() }
        refresh(tabs)

        MaterialTheme {
            LazyRow(state = state, contentPadding = PaddingValues(horizontal = 4.dp)) {
                itemsIndexed(tabs) { position, tab ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.tab_list_item_width))
                            .height(dimensionResource(R.dimen.tab_list_item_height))
                            .clickable {
                                callback?.replaceTabFromTabList(tab)
                                callback?.onCloseOnly()
                            }
                            .background(
                                if (index == position)
                                    Color(ColorUtils.setAlphaComponent(colorPair.bgColor(), 128))
                                else
                                    Color.Transparent
                            )
                    ){
                        Surface(
                            elevation = 4.dp,
                            modifier = Modifier
                                .width(112.dp)
                                .height(152.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(112.dp)
                                    .height(152.dp)
                                    .padding(4.dp)
                                    .align(Alignment.BottomCenter)
                            ) {
                                AsyncImage(
                                    model = tabThumbnails.assignNewFile(tab.thumbnailPath()),
                                    contentDescription = tab.title(),
                                    contentScale = ContentScale.FillHeight,
                                    placeholder = painterResource(id = R.drawable.ic_yobidashi),
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .align(Alignment.TopCenter)
                                )
                                Text(
                                    text = tab.title(),
                                    color = Color(colorPair.fontColor()),
                                    maxLines = 2,
                                    fontSize = 14.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(Color(colorPair.bgColor()))
                                        .padding(4.dp)
                                )
                            }
                        }

                        Icon(
                            painterResource(id = R.drawable.ic_remove_circle),
                            tint = Color(colorPair.fontColor()),
                            contentDescription = stringResource(id = R.string.delete),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clickable {
                                    val removeIndex =
                                        callback?.tabIndexOfFromTabList(tab) ?: return@clickable
                                    callback?.closeTabFromTabList(removeIndex)
                                    refresh(tabs)
                                }
                        )
                    }
                }
            }
        }

        /*

        adapter = Adapter(activityContext, callback as Callback)
        adapter.setCurrentIndex(index)

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

        BackgroundImageLoaderUseCase().invoke(
                binding.background,
                PreferenceApplier(activityContext).backgroundImagePath
        )
         */
    }

    private fun refresh(tabs: SnapshotStateList<Tab>) {
        tabs.clear()

        (0 until (callback?.getTabAdapterSizeFromTabList() ?: 0)).forEach {
            val tab = callback?.getTabByIndexFromTabList(it) ?: return@forEach
            tabs.add(tab)
        }
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
