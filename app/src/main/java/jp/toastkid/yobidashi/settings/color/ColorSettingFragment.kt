package jp.toastkid.yobidashi.settings.color

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Color setting activity.
 *
 * TODO clean up with ViewModel.
 *
 * @author toastkidjp
 */
class ColorSettingFragment : Fragment(), CommonFragmentAction {

    /**
     * Initial background color.
     */
    private var initialBgColor: Int = 0

    /**
     * Initial font color.
     */
    private var initialFontColor: Int = 0

    private var currentBackgroundColor: MutableState<Color>? = null

    private var currentFontColor: MutableState<Color>? = null

    /**
     * Saved color's adapter.
     */
    private var adapter: SavedColorAdapter? = null

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var repository: SavedColorRepository

    /**
     * Subscribed disposables.
     */
    private val disposables: Job by lazy { Job() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return null
        preferenceApplier = PreferenceApplier(context)

        setHasOptionsMenu(true)

        repository = DatabaseFinder().invoke(context).savedColorRepository()

        val colorPair = colorPair()
        initialBgColor = colorPair.bgColor()
        initialFontColor = colorPair.fontColor()

        return ComposeViewFactory().invoke(context) {
            val coroutineScope = rememberCoroutineScope()

            MaterialTheme() {
                LazyColumn(
                    modifier = Modifier.nestedScroll(rememberViewInteropNestedScrollConnection())
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    item {
                        val currentBackgroundColor =
                            remember { mutableStateOf(Color(preferenceApplier.color)) }
                        this@ColorSettingFragment.currentBackgroundColor = currentBackgroundColor

                        val currentFontColor =
                            remember { mutableStateOf(Color(preferenceApplier.fontColor)) }
                        this@ColorSettingFragment.currentFontColor = currentFontColor

                        Surface(
                            elevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                //.requiredHeight(300.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(id = R.string.settings_color_background_title),
                                        fontSize = 16.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    HarmonyColorPicker(
                                        harmonyMode = ColorHarmonyMode.COMPLEMENTARY,
                                        color = currentBackgroundColor.value,
                                        onColorChanged = { hsvColor ->
                                            currentBackgroundColor.value = hsvColor.toColor()
                                        },
                                        modifier = Modifier.height(200.dp)
                                    )
                                    Button(
                                        onClick = {
                                            val bgColor = currentBackgroundColor.value
                                            val fontColor = currentFontColor.value

                                            commitNewColor(bgColor, fontColor)

                                            CoroutineScope(Dispatchers.Main).launch(disposables) {
                                                withContext(Dispatchers.IO) {
                                                    val savedColor =
                                                        SavedColor.make(
                                                            bgColor.toArgb(),
                                                            fontColor.toArgb()
                                                        )
                                                    repository.add(savedColor)
                                                    adapter?.reload()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            backgroundColor = currentBackgroundColor.value,
                                            contentColor = Color(preferenceApplier.fontColor),
                                            disabledContentColor = Color.LightGray
                                        ),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(stringResource(id = R.string.commit), fontSize = 14.sp)
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .wrapContentHeight()
                                ) {
                                    Text(
                                        stringResource(id = R.string.settings_color_font_title),
                                        fontSize = 16.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    HarmonyColorPicker(
                                        harmonyMode = ColorHarmonyMode.COMPLEMENTARY,
                                        color = currentFontColor.value,
                                        modifier = Modifier.height(200.dp),
                                        onColorChanged = { hsvColor ->
                                            currentFontColor.value = hsvColor.toColor()
                                        }
                                    )
                                    Button(
                                        onClick = {
                                            commitNewColor(
                                                Color(initialBgColor),
                                                Color(initialFontColor)
                                            )

                                            activity?.let { Updater().update(it) }
                                            snackShort(R.string.settings_color_done_reset)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            backgroundColor = Color(initialBgColor),
                                            contentColor = Color(initialFontColor),
                                            disabledContentColor = Color.LightGray
                                        ),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(stringResource(id = R.string.reset), fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    coroutineScope.launch {
                        val savedColors = withContext(Dispatchers.IO) { repository.findAll().windowed(3, 3) }
                        if (savedColors.isEmpty()) {
                            return@launch
                        }

                        item {
                            Surface(
                                elevation = 4.dp,
                                modifier = Modifier.height(44.dp).fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.settings_color_saved_title),
                                    fontSize = 18.sp
                                )
                            }
                        }

                        items(savedColors) { savedColorRow ->
                            Row {
                                savedColorRow.forEach { savedColor ->
                                    Surface(
                                        elevation = 4.dp,
                                        modifier = Modifier
                                            .clickable {
                                                commitNewColor(
                                                    Color(savedColor.bgColor),
                                                    Color(savedColor.fontColor)
                                                )
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Box(modifier = Modifier
                                            .weight(1f)
                                            .height(100.dp)
                                            .background(Color(savedColor.bgColor))
                                            .padding(8.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.sample_a),
                                                color = Color(savedColor.fontColor),
                                                modifier = Modifier.align(
                                                Alignment.Center)
                                            )
                                            Icon(painterResource(id = R.drawable.ic_remove_circle), contentDescription = stringResource(
                                                id = R.string.delete
                                            ),
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(40.dp)
                                                    .clickable {
                                                        coroutineScope.launch {
                                                            withContext(Dispatchers.IO) {
                                                                repository.delete(savedColor)
                                                            }
                                                        }
                                                    }
                                                    .align(Alignment.TopEnd))
                                        }
                                    }
                                }
                            }

                            /*
                            binding?.clearSavedColor?.setOnClickListener{
            ConfirmDialogFragment.show(
                parentFragmentManager,
                getString(R.string.title_clear_saved_color),
                Html.fromHtml(
                    activityContext.getString(R.string.confirm_clear_all_settings),
                    Html.FROM_HTML_MODE_COMPACT
                ),
                "clear_color"
            )
        }
                             */
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "clear_color",
            viewLifecycleOwner,
            { _, _ ->
                adapter?.clear()
            }
        )
        parentFragmentManager.setFragmentResultListener(
            "add_recommended_colors",
            viewLifecycleOwner,
            { _, _ -> DefaultColorInsertion().insert(view.context) }
        )
    }

    /**
     * Commit new color.
     *
     * @param bgColor   Background color int
     * @param fontColor Font color int
     */
    private fun commitNewColor(bgColor: Color, fontColor: Color) {
        preferenceApplier.color = bgColor.toArgb()
        preferenceApplier.fontColor = fontColor.toArgb()

        currentBackgroundColor?.value = bgColor
        currentFontColor?.value = fontColor

        activity?.let { Updater().update(it) }

        snackShort(R.string.settings_color_done_commit)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.color_setting_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.color_settings_toolbar_menu_add_recommend -> {
                ConfirmDialogFragment.show(
                    parentFragmentManager,
                    getString(R.string.title_add_recommended_colors),
                    getString(R.string.message_add_recommended_colors),
                    "add_recommended_colors"
                )
                true
            }
            R.id.color_settings_toolbar_menu_add_random -> {
                RandomColorInsertion(repository)() {
                    adapter?.refresh()
                }
                snackShort(R.string.done_addition)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun snackShort(@StringRes messageId: Int) {
        view?.let {
            Toaster.snackShort(it, messageId, colorPair())
        }
    }

    private fun colorPair() = preferenceApplier.colorPair()

    override fun onDetach() {
        disposables.cancel()
        parentFragmentManager.clearFragmentResultListener("clear_color")
        parentFragmentManager.clearFragmentResultListener("add_recommended_colors")
        super.onDetach()
    }

    companion object : TitleIdSupplier {

        override fun titleId(): Int = R.string.title_settings_color

    }
}
