/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.list

import androidx.recyclerview.widget.DiffUtil

class CommonItemCallback<T> private constructor(
    private val sameItemComparator: (T, T) -> Boolean,
    private val equals: (T, T) -> Boolean
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return sameItemComparator(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return equals(oldItem, newItem)
    }

    companion object {

        fun <T>with(
            sameItemComparator: (T, T) -> Boolean,
            equals: (T, T) -> Boolean
        ): DiffUtil.ItemCallback<T> =
            CommonItemCallback(sameItemComparator, equals)

    }
}
