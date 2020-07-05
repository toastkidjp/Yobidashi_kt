/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab.model

import androidx.annotation.Keep
import java.util.UUID

/**
 * @author toastkidjp
 */
class ArticleTab : Tab {

    @Keep
    private val articleTab = true

    private var id = UUID.randomUUID().toString()

    private var titleStr = ""

    private var contentStr = ""

    private var scrollY = 0

    override fun id() = id

    override fun setScrolled(scrollY: Int) {
        this.scrollY = scrollY
    }

    override fun getScrolled(): Int = scrollY

    override fun title() = titleStr

    fun setTitle(title: String) {
        titleStr = title
    }

    fun content() = contentStr

    fun setContent(content: String) {
        contentStr = content
    }

    companion object {
        fun make(title: String, content: String): ArticleTab {
            val articleTab = ArticleTab()
            articleTab.setTitle(title)
            articleTab.setContent(content)
            return articleTab
        }
    }
}