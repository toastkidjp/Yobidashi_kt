/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.menu

import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.databinding.PopupArticleListMenuBinding

class MenuPopupImplementation(context: Context) : MenuPopupView {

    private val binding: PopupArticleListMenuBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.popup_article_list_menu,
        null,
        false
    )

    override fun setPopup(popup: MenuPopup) {
        binding.popup = popup
    }

    override fun setVisibility(useAddToBookmark: Boolean) {
        binding.addToBookmark.isVisible = useAddToBookmark
        binding.divider.isVisible = useAddToBookmark
    }

    override fun contentView() = binding.root

}