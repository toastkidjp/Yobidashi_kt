/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

class MonthFormatterUseCase {

    operator fun invoke(month: Int): String {
        return  if (month < 9) "0${month + 1}" else "${month + 1}"
    }

}