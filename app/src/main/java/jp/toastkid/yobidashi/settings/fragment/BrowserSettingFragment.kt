/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebViewFeature
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.libs.Toaster
import kotlin.math.roundToInt

/**
 * Setting fragment of WEB browser.
 *
 * @author toastkidjp
 */
class BrowserSettingFragment : Fragment() {

    /**
     * Preference wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    private var userAgent: MutableState<String>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val activityContext = activity
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
        this.contentViewModel = contentViewModel

        parentFragmentManager.setFragmentResultListener(
            "user_agent_setting",
            viewLifecycleOwner,
            { key, results ->
                val userAgent = results[key] as? UserAgent ?: return@setFragmentResultListener
                this.userAgent?.value = userAgent.title()
            }
        )

        val iconTint = Color(IconColorFinder.from(activityContext).invoke())

        return ComposeViewFactory().invoke(activityContext) {
            val retainTabs = remember { mutableStateOf(preferenceApplier.doesRetainTabs()) }
            val homeUrl = remember { mutableStateOf(preferenceApplier.homeUrl) }
            val useJavaScript = remember { mutableStateOf(preferenceApplier.useJavaScript()) }
            val useImage = remember { mutableStateOf(preferenceApplier.doesLoadImage()) }
            val saveFormData = remember { mutableStateOf(preferenceApplier.doesSaveForm()) }
            val saveViewHistory = remember { mutableStateOf(preferenceApplier.saveViewHistory) }
            val userAgent = remember { mutableStateOf(preferenceApplier.userAgent()) }
            this.userAgent = userAgent
            val poolSize = remember { mutableStateOf(preferenceApplier.poolSize.toFloat() / 30f) }
            val backgroundAlpha = remember { mutableStateOf(preferenceApplier.getWebViewBackgroundAlpha()) }
            val useDarkMode = remember { mutableStateOf(preferenceApplier.useDarkMode()) }
            val useAdRemover = remember { mutableStateOf(preferenceApplier.adRemove) }

            MaterialTheme() {
                Surface(elevation = 4.dp, modifier = Modifier.padding(16.dp)) {
                    LazyColumn(
                        Modifier
                            .background(colorResource(id = R.color.setting_background))
                            .nestedScroll(rememberViewInteropNestedScrollConnection())
                    ) {
                        item {
                            CheckableRow(
                                retainTabs,
                                {
                                    switchRetainTabs()
                                    retainTabs.value = preferenceApplier.doesRetainTabs()
                                },
                                R.string.title_enable_javascript,
                                iconTint,
                                R.drawable.ic_tab_black
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_home_black),
                                    tint = iconTint,
                                    contentDescription = stringResource(id = R.string.title_retain_tabs)
                                )
                                TextField(
                                    value = homeUrl.value,
                                    onValueChange = {
                                        homeUrl.value = it
                                    }, 
                                    label = { stringResource(id = R.string.title_home) },
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (homeUrl.value.isEmpty()) {
                                            contentViewModel.snackShort(
                                                R.string.favorite_search_addition_dialog_empty_message
                                            )
                                            return@KeyboardActions
                                        }
                                        if (Urls.isInvalidUrl(homeUrl.value)) {
                                            contentViewModel.snackShort(
                                                R.string.message_invalid_url
                                            )
                                            return@KeyboardActions
                                        }
                                        preferenceApplier.homeUrl = homeUrl.value

                                        contentViewModel.snackShort(
                                            getString(R.string.message_commit_home, homeUrl.value)
                                        )
                                    }),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            CheckableRow(
                                useJavaScript,
                                {
                                    switchJsEnabled()
                                    useJavaScript.value = preferenceApplier.useJavaScript()
                                },
                                R.string.title_enable_javascript
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            CheckableRow(
                                useImage,
                                {
                                    this@BrowserSettingFragment.switchLoadingImage()
                                    useImage.value =
                                        this@BrowserSettingFragment.preferenceApplier.doesLoadImage()
                                },
                                R.string.title_load_image,
                                iconTint,
                                R.drawable.ic_image
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            CheckableRow(
                                saveFormData,
                                {
                                    switchSaveFormData()
                                    saveFormData.value =
                                        this@BrowserSettingFragment.preferenceApplier.doesSaveForm()
                                },
                                R.string.title_save_form_data
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            CheckableRow(
                                saveViewHistory,
                                {
                                    switchViewHistory()
                                    saveViewHistory.value = preferenceApplier.doesSaveForm()
                                },
                                R.string.title_save_view_history,
                                iconTint,
                                R.drawable.ic_open_in_browser_black
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable(onClick = {
                                        userAgent()
                                        //TODO
                                    })
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_user_agent_black),
                                    tint = iconTint,
                                    contentDescription = stringResource(id = R.string.title_user_agent)
                                )

                                Text(
                                    stringResource(id = R.string.title_user_agent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )

                                Text(
                                    userAgent.value,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Column(
                                modifier = Modifier.height(dimensionResource(id = R.dimen.settings_item_height))
                                    .padding(start = 16.dp, end = 16.dp)
                            ) {
                                Row() {
                                    Text(stringResource(id = R.string.title_tab_retaining_size))
                                    Text(
                                        "${((30) * poolSize.value).roundToInt()}",
                                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                                    )
                                }
                                Slider(
                                    value = poolSize.value,
                                    onValueChange = {
                                        val newValue = ((30) * it).roundToInt()
                                        poolSize.value = it
                                        preferenceApplier.poolSize = newValue
                                    },
                                    steps = 30,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Column(
                                modifier = Modifier.height(dimensionResource(id = R.dimen.settings_item_height))
                                    .padding(start = 16.dp, end = 16.dp)
                            ) {
                                Row() {
                                    Text("Background alpha")
                                    Text(
                                        "${backgroundAlpha.value}",
                                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                                    )
                                }
                                Slider(
                                    value = backgroundAlpha.value,
                                    onValueChange = {
                                        backgroundAlpha.value = it
                                        preferenceApplier.setWebViewBackgroundAlpha(it)
                                    },
                                    steps = 100,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                                CheckableRow(
                                    useDarkMode,
                                    {
                                        switchDarkMode()
                                        useDarkMode.value = preferenceApplier.useDarkMode()
                                    },
                                    R.string.title_dark_mode,
                                    iconTint,
                                    R.drawable.ic_dark_mode_black
                                )
                            }
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            CheckableRow(
                                useAdRemover,
                                {
                                    switchAdRemove()
                                    useAdRemover.value = preferenceApplier.adRemove
                                },
                                R.string.title_remove_ad,
                                iconTint,
                                R.drawable.ic_block_black
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Text(
                                stringResource(id = R.string.title_clear_cache),
                                modifier = Modifier.padding(16.dp)
                                    .clickable {
                                        val view = view ?: return@clickable
                                        clearCache(view)
                                    }
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Text(
                                stringResource(id = R.string.title_clear_form_data),
                                modifier = Modifier.padding(16.dp)
                                    .clickable {
                                        val view = view ?: return@clickable
                                        clearFormData(view)
                                    }
                            )
                        }

                        item {
                            InsetDivider()
                        }

                        item {
                            Text(
                                stringResource(id = R.string.title_clear_cache),
                                modifier = Modifier.padding(16.dp)
                                    .clickable {
                                        val view = view ?: return@clickable
                                        clearCookie(view)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CheckableRow(
        booleanState: MutableState<Boolean>,
        clickable: () -> Unit,
        textId: Int,
        iconTint: Color? = null,
        iconId: Int? = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = clickable)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            if (iconId != null && iconTint != null) {
                Icon(
                    painterResource(id = iconId),
                    tint = iconTint,
                    contentDescription = stringResource(id = textId),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Text(
                stringResource(id = textId),
                modifier = Modifier
                    .weight(1f)
            )
            Checkbox(
                checked = booleanState.value, onCheckedChange = {},
                modifier = Modifier.width(44.dp)
            )
        }
    }

    /**
     * Switch retaining tabs.
     */
    fun switchRetainTabs() {
        val newState = !preferenceApplier.doesRetainTabs()
        preferenceApplier.setRetainTabs(newState)
        @StringRes val messageId: Int = if (newState)
            R.string.message_check_retain_tabs
        else
            R.string.message_check_doesnot_retain_tabs
        contentViewModel?.snackShort(messageId)
    }

    /***
     * Commit input.
     * TODO Delete it.
     */
    fun commitHomeInput(input: String?) {
        if (input.isNullOrEmpty()) {
            contentViewModel?.snackShort(R.string.favorite_search_addition_dialog_empty_message)
            return
        }
        if (Urls.isInvalidUrl(input)) {
            contentViewModel?.snackShort(R.string.message_invalid_url)
            return
        }
        preferenceApplier.homeUrl = input

        contentViewModel?.snackShort(getString(R.string.message_commit_home, input))
    }

    /**
     * Switch Wi-Fi only mode.
     */
    fun switchDarkMode() {
        val newState = !preferenceApplier.useDarkMode()
        preferenceApplier.setUseDarkMode(newState)
    }

    /**
     * Switch JavaScript enabling.
     */
    fun switchJsEnabled() {
        val preferenceApplier = preferenceApplier
        val newState = !preferenceApplier.useJavaScript()
        preferenceApplier.setUseJavaScript(newState)
        @StringRes val messageId: Int = if (newState)
            R.string.message_js_enabled
        else
            R.string.message_js_disabled
        contentViewModel?.snackShort(messageId)
    }


    /**
     * Switch loading images.
     */
    fun switchLoadingImage() {
        val newState = !preferenceApplier.doesLoadImage()
        preferenceApplier.setLoadImage(newState)
    }

    /**
     * Switching saving form data.
     */
    fun switchSaveFormData() {
        val newState = !preferenceApplier.doesSaveForm()
        preferenceApplier.setSaveForm(newState)
    }

    /**
     * Switch saving view history.
     */
    fun switchViewHistory() {
        val newState = !preferenceApplier.saveViewHistory
        preferenceApplier.saveViewHistory = newState
    }

    /**
     * Switch AD remover setting.
     */
    fun switchAdRemove() {
        val newState = !preferenceApplier.adRemove
        preferenceApplier.adRemove = newState
    }

    /**
     * Clear [WebView] cache.
     *
     * @param snackbarParent for data binding
     */
    fun clearCache(snackbarParent: View) {
        WebView(snackbarParent.context).clearCache(true)
        Toaster.snackShort(snackbarParent, R.string.done_clear, preferenceApplier.colorPair())
    }

    /**
     * Clear [WebView] form data.
     *
     * @param snackbarParent for data binding
     */
    fun clearFormData(snackbarParent: View) {
        WebView(snackbarParent.context).clearFormData()
        contentViewModel?.snackShort(R.string.done_clear)
    }

    /**
     * Clear all cookie.
     *
     * @param snackbarParent for data binding
     */
    fun clearCookie(snackbarParent: View) {
        CookieManager.getInstance().removeAllCookies {
            contentViewModel?.snackShort(R.string.done_clear)
        }
    }

    /**
     * UserAgent setting.
     */
    fun userAgent() {
        val dialogFragment = UserAgentDialogFragment()
        dialogFragment.show(
                parentFragmentManager,
                UserAgentDialogFragment::class.java.simpleName
        )
    }

    override fun onDetach() {
        parentFragmentManager.clearFragmentResultListener("user_agent_setting")
        super.onDetach()
    }

    companion object : TitleIdSupplier {

        @StringRes
        override fun titleId() = R.string.subhead_setting_browser

    }
}