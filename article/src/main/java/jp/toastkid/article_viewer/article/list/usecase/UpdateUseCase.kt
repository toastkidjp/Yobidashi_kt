/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.usecase

import android.content.Context
import android.net.Uri
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import jp.toastkid.article_viewer.zip.ZipLoaderService

class UpdateUseCase(
    private val viewModel: ArticleListFragmentViewModel?,
    private val contextProvider: () -> Context?
) {

    fun invokeIfNeed(target: Uri?) {
        if (target == null) {
            return
        }

        viewModel?.showProgress()

        contextProvider.invoke()?.let {
            ZipLoaderService.start(it, target)
        }
    }
}