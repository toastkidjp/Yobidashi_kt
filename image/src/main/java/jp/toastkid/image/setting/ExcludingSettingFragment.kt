/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.setting

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.image.R
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.list.SwipeToDismissItem

/**
 * @author toastkidjp
 */
class ExcludingSettingFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return null
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent { ExclusionSettingUi() }

        return composeView
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ExclusionSettingUi() {
        val preferenceApplier = PreferenceApplier(LocalContext.current)
        val list = preferenceApplier.excludedItems().toList()

        LazyColumn {
            item {
                Surface(
                    elevation = 4.dp,
                    modifier = Modifier.height(48.dp).padding(bottom = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "Excluding items",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                        )
                    }
                }
            }

            items(list) { item ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart){
                            preferenceApplier.removeFromExcluding(item)
                        }
                        true
                    }
                )
                SwipeToDismissItem(
                    dismissState = dismissState,
                    dismissContent = {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()) {
                            Text(
                                text = item,
                                fontSize = 16.sp,
                                maxLines = 3,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            AsyncImage(
                                R.drawable.ic_remove_circle,
                                contentDescription = item,
                                modifier = Modifier
                                    .width(32.dp)
                                    .fillMaxHeight()
                            )
                        }
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenceApplier = PreferenceApplier(view.context)
        val excludedItems = preferenceApplier.excludedItems()
        if (excludedItems.isEmpty()) {
            dismiss()
            return
        }

        val viewModel = ViewModelProvider(this)
                .get(ExcludingSettingFragmentViewModel::class.java)

        viewModel.dismiss.observe(this, {
            dismiss()
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult("excluding", Bundle.EMPTY)
    }

}