/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.chat.domain.repository

import jp.toastkid.chat.domain.model.Source

data class ChatResponseItem(
    private val message: String,
    private val error: Boolean = false,
    private val image: Boolean = false,
    private val sources: List<Source> = emptyList<Source>(),
    ) {

    fun message() = message

    fun error() = error

    fun image() = image

    fun sources() = sources

    companion object {

        private val ERROR = ChatResponseItem("[ERROR]", error = true)

        private val OTHER = ChatResponseItem("[OTHER]", error = true)

        fun error() = ERROR

    }

}