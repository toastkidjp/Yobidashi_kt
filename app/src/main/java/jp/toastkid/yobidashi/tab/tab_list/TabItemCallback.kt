/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.tab_list

import androidx.recyclerview.widget.DiffUtil
import jp.toastkid.yobidashi.tab.model.Tab

internal class TabItemCallback : DiffUtil.ItemCallback<Tab>() {

    override fun areItemsTheSame(oldItem: Tab, newItem: Tab): Boolean {
        return oldItem.id() == newItem.id()
    }

    override fun areContentsTheSame(oldItem: Tab, newItem: Tab): Boolean {
        return oldItem.equals(newItem)
    }
}