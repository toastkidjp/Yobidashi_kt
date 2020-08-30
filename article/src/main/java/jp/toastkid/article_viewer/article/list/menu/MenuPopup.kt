/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.SearchResult
import jp.toastkid.article_viewer.bookmark.Bookmark
import jp.toastkid.article_viewer.databinding.PopupArticleListMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class MenuPopup(context: Context, private val deleted: () -> Unit) {

    private val popupWindow = PopupWindow(context)

    private val database = AppDatabase.find(context)

    private val articleRepository = database.articleRepository()

    private val bookmarkRepository = database.bookmarkRepository()

    private val binding: PopupArticleListMenuBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.popup_article_list_menu,
            null,
            false
    )

    private var targetId: Int? = null

    init {
        popupWindow.contentView = binding.root
        popupWindow.isOutsideTouchable = true
        popupWindow.width = context.resources.getDimensionPixelSize(R.dimen.menu_popup_width)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        binding.popup = this
    }

    fun show(view: View, searchResult: SearchResult) {
        targetId = searchResult.id
        popupWindow.showAsDropDown(view)
    }

    fun addToBookmark() {
        targetId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                bookmarkRepository.add(Bookmark(it))
            }
        }
        popupWindow.dismiss()
    }

    fun delete() {
        targetId?.let {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    articleRepository.delete(it)
                }
                deleted()
            }
        }
        popupWindow.dismiss()
    }

}