/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import jp.toastkid.lib.viewmodel.event.Event

/**
 * @author toastkidjp
 */
data class SnackbarEvent(
        val message: String? = null,
        val messageId: Int? = null,
        val actionLabel: String? = null,
        val action: () -> Unit = {}
) : Event