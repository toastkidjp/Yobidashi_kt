/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.domain.model

interface TwoStringConverter {

    fun title(): String

    fun firstInputLabel(): String

    fun secondInputLabel(): String

    fun defaultFirstInputValue(): String

    fun defaultSecondInputValue(): String

    fun firstInputAction(input: String): String?

    fun secondInputAction(input: String): String?

}