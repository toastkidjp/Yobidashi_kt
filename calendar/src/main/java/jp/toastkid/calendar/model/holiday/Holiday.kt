/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.model.holiday

data class Holiday(
    val title: String,
    val month: Int,
    val day: Int,
    val flag: String = "\uD83C\uDDFA\uD83C\uDDF8"
)