/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.category

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * @author toastkidjp
 */
class ViewHolder(private val icon: ImageView, private val text: TextView) {

    fun bindItem(searchCategory: SearchCategory) {
        icon.setImageDrawable(AppCompatResources.getDrawable(icon.context, searchCategory.iconId))
        text.setText(searchCategory.id)
    }
}