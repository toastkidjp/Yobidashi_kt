/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/**
 * @author toastkidjp
 */
@Fts4(contentEntity = Article::class, tokenizer = FtsOptions.TOKENIZER_PORTER)
@Entity(tableName = "articleFts")
class ArticleFts {
    @Keep
    var bigram: String = ""
}