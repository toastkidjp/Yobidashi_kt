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
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import kotlin.math.roundToInt

/**
 * @author toastkidjp
 */
class ColorFilterSettingFragment : Fragment() {

    private lateinit var preferenceApplier: PreferenceApplier

    private var overlayColorFilterViewModel: OverlayColorFilterViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityContext = context
            ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        val useCase = OverlayColorFilterUseCase(
            preferenceApplier,
            { ContextCompat.getColor(activityContext, it) },
            overlayColorFilterViewModel
        )

        activity?.let { activity ->
            overlayColorFilterViewModel =
                ViewModelProvider(activity).get(OverlayColorFilterViewModel::class.java)
        }

        return ComposeViewFactory().invoke(activityContext) {
            val sample =
                remember { mutableStateOf(preferenceApplier.filterColor(Color.Transparent.toArgb())) }
            val sliderValue =
                remember { mutableStateOf(OverlayColorFilterUseCase.getDefaultAlpha().toFloat()) }
            val check = remember { mutableStateOf(preferenceApplier.useColorFilter()) }

            overlayColorFilterViewModel
                ?.newColor
                ?.observe(
                    viewLifecycleOwner,
                    { sample.value = preferenceApplier.filterColor(Color.Transparent.toArgb()) }
                )

            MaterialTheme() {
                Surface(elevation = 4.dp, modifier = Modifier.padding(16.dp)) {
                    Column(Modifier.background(colorResource(id = R.color.setting_background))) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    switchColorFilter()
                                    check.value = preferenceApplier.useColorFilter()
                                }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_color_filter_black),
                                contentDescription = stringResource(id = R.string.title_color_filter),
                                tint = Color(IconColorFinder.from(activityContext).invoke())
                            )
                            Text(
                                text = stringResource(id = R.string.title_color_filter),
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = check.value,
                                onCheckedChange = {},
                                modifier = Modifier
                                    .width(44.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }

                        InsetDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.sample_text_color_filter),
                                color = colorResource(id = R.color.black),
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(sample.value))
                            )
                            Text(
                                text = stringResource(id = R.string.title_filter_color),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp, end = 4.dp)
                            )
                            Button(
                                onClick = {
                                    useCase.setDefault()
                                    sliderValue.value = OverlayColorFilterUseCase
                                        .getDefaultAlpha()
                                        .toFloat()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    backgroundColor = Color(preferenceApplier.color),
                                    contentColor = Color(preferenceApplier.fontColor),
                                    disabledContentColor = Color.LightGray
                                ),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.title_default),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        InsetDivider()

                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            ColorFilterItem(R.color.default_color_filter) { useCase.setYellow() }
                            ColorFilterItem(R.color.red_yellow) { useCase.setRedYellow() }
                            ColorFilterItem(R.color.deep_orange_500_dd) { useCase.setOrange() }
                            ColorFilterItem(R.color.darkgray_scale) { useCase.setDark() }
                            ColorFilterItem(R.color.red_200_dd) { useCase.setRed() }
                            ColorFilterItem(R.color.lime_bg) { useCase.setGreen() }
                            ColorFilterItem(R.color.light_blue_200_dd) { useCase.setBlue() }
                        }

                        Slider(
                            value = sliderValue.value,
                            onValueChange = {
                                sliderValue.value = it
                                useCase.setAlpha(((255) * it).roundToInt())
                            },
                            steps = 256,
                            modifier = Modifier.padding(
                                top = 8.dp,
                                bottom = 8.dp,
                                start = 16.dp,
                                end = 16.dp
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ColorFilterItem(@ColorRes colorId: Int, onClick: () -> Unit) {
        Surface(elevation = 4.dp) {
            Box(
                modifier = Modifier
                    .background(colorResource(id = colorId))
                    .size(40.dp)
                    .clickable(onClick = onClick)
                    .padding(start = 8.dp)
            ) { }
        }
    }

    /**
     * Switch color filter's visibility.
     */
    fun switchColorFilter() {
        val newState = !preferenceApplier.useColorFilter()
        preferenceApplier.setUseColorFilter(newState)
        overlayColorFilterViewModel?.update()
    }

    companion object : TitleIdSupplier {

        @StringRes
        override fun titleId() = R.string.title_color_filter

    }
}