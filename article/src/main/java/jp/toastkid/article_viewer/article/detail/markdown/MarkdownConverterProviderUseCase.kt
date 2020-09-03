/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.markdown

import android.content.Context
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin

/**
 * @author toastkidjp
 */
class MarkdownConverterProviderUseCase {

    operator fun invoke(context: Context): Markwon {
        val plugins = listOf(TablePlugin.create(context), TaskListPlugin.create(context))
        return Markwon.builder(context).usePlugins(plugins).build()
    }

}