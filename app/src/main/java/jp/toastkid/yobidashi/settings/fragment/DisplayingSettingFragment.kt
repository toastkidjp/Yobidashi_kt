/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.DarkModeApplier
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.background.load.LoadedAction
import java.io.File

/**
 * Display setting fragment.
 *
 * @author toastkidjp
 */
class DisplayingSettingFragment : Fragment() {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    /**
     * Wrapper of FilesDir.
     */
    private lateinit var filesDir: FilesDir

    private var files: MutableState<List<List<File>>>? = null

    private val addingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) {
            return@registerForActivityResult
        }

        val view = view ?: return@registerForActivityResult

        LoadedAction(
            it.data?.data,
            view,
            preferenceApplier.colorPair(),
            { refresh() },
            BACKGROUND_DIR
        )
            .invoke()
    }

    private var lastSnackbar: Snackbar? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val activityContext = activity
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
        setHasOptionsMenu(true)

        filesDir = FilesDir(activityContext, BACKGROUND_DIR)

        return ComposeViewFactory().invoke(activityContext) {
            val iconColor = Color(IconColorFinder.from(activityContext).invoke())

            val files = remember {
                mutableStateOf(filesDir.listFiles().toList().windowed(2, 2, true))
            }
            this@DisplayingSettingFragment.files = files

            MaterialTheme() {
                Surface(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
                    LazyColumn(
                        Modifier.background(colorResource(id = R.color.setting_background))
                                .nestedScroll(rememberViewInteropNestedScrollConnection())
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable { applyDarkMode() }
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_dark_mode_black),
                                    tint = iconColor,
                                    contentDescription = stringResource(id = R.string.apply_dark_mode)
                                )
                                Text(
                                    text = stringResource(id = R.string.apply_dark_mode),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp)
                                )
                            }

                            InsetDivider()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable { removeBackgroundSettings() }
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_close_black),
                                    tint = iconColor,
                                    contentDescription = stringResource(id = R.string.title_bg_reset)
                                )
                                Text(
                                    text = stringResource(id = R.string.title_bg_reset),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp)
                                )
                            }

                            InsetDivider()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_image),
                                    tint = iconColor,
                                    contentDescription = stringResource(id = R.string.title_background_image_setting)
                                )
                                Text(
                                    text = stringResource(id = R.string.title_background_image_setting),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                                Icon(
                                    painterResource(id = R.drawable.ic_add_white),
                                    tint = iconColor,
                                    contentDescription = stringResource(id = R.string.add_background_image),
                                    modifier = Modifier.clickable { launchAdding() }
                                )
                            }
                        }

                        if (files.value.isEmpty()) {
                            return@LazyColumn
                        }

                        items(files.value) { column ->
                            Row() {
                                column.forEach { imageFile ->
                                    Surface(elevation = 4.dp, modifier = Modifier
                                        .height(200.dp)
                                        .weight(1f)
                                        .padding(4.dp)) {
                                        Box(Modifier.clickable {
                                            preferenceApplier.backgroundImagePath = imageFile.path
                                            contentViewModel
                                                ?.snackShort(R.string.message_change_background_image)
                                        }) {
                                        Column() {
                                            AsyncImage(
                                                model = imageFile,
                                                contentDescription = imageFile.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .height(150.dp)
                                                    .padding(4.dp)
                                            )
                                            Text(
                                                imageFile.nameWithoutExtension,
                                                maxLines = 2,
                                                fontSize = 14.sp,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                            Icon(
                                                painterResource(id = R.drawable.ic_remove_circle),
                                                contentDescription = stringResource(id = R.string.delete),
                                                modifier = Modifier.size(40.dp).align(Alignment.TopEnd)
                                                    .clickable {
                                                        if (!imageFile.exists()) {
                                                            contentViewModel
                                                                ?.snackShort(R.string.message_cannot_found_image)
                                                            return@clickable
                                                        }
                                                        val successRemove = imageFile.delete()
                                                        if (!successRemove) {
                                                            contentViewModel
                                                                ?.snackShort(R.string.message_failed_image_removal)
                                                            return@clickable
                                                        }
                                                        refresh()
                                                        contentViewModel
                                                            ?.snackShort(R.string.message_success_image_removal)
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "clear_background_images",
            viewLifecycleOwner,
            { _, _ ->
                filesDir.clean()
                contentViewModel?.snackShort(R.string.message_success_image_removal)
                refresh()
            }
        )
    }

    private fun refresh() {
        files?.value = filesDir.listFiles().toList().windowed(2, 2, true)
    }

    fun applyDarkMode() {
        view?.let {
            DarkModeApplier().invoke(preferenceApplier, it)
        }
    }

    /**
     * Clear background setting.
     */
    fun removeBackgroundSettings() {
        preferenceApplier.removeBackgroundImagePath()
        contentViewModel?.snackShort(R.string.message_reset_bg_image)
    }

    /**
     * Launch Adding action.
     */
    fun launchAdding() {
        addingLauncher.launch(makePickImage())
    }

    /**
     * Make pick image intent.
     * @return Intent
     */
    private fun makePickImage(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        return intent
    }

    /**
     * Clear all images.
     */
    private fun clearImages() {
        ConfirmDialogFragment.show(
            parentFragmentManager,
            getString(R.string.clear_all),
            Html.fromHtml(
                getString(R.string.confirm_clear_all_settings),
                Html.FROM_HTML_MODE_COMPACT
            ),
            "clear_background_images"
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.background_setting_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.background_settings_toolbar_menu_add -> {
            launchAdding()
            true
        }
        R.id.background_settings_add_default -> {
            activity?.let { activity ->
                DefaultBackgroundImagePreparation().invoke(activity) {
                    activity.runOnUiThread { refresh() }
                }
            }
            true
        }
        R.id.background_settings_toolbar_menu_clear -> {
            clearImages()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        lastSnackbar?.dismiss()
        super.onPause()
    }

    override fun onDetach() {
        lastSnackbar?.dismiss()
        contentViewModel?.refresh()
        addingLauncher.unregister()

        parentFragmentManager.clearFragmentResultListener("clear_background_images")

        super.onDetach()
    }

    companion object : TitleIdSupplier {

        /**
         * Background image dir.
         */
        private const val BACKGROUND_DIR: String = "background_dir"

        fun getBackgroundDirectory() = BACKGROUND_DIR

        @StringRes
        override fun titleId(): Int = R.string.title_settings_display

    }
}