/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.domain.model

class TatamiCountConverter : TwoStringConverter {

    override fun title(): String {
        return "Tatami counts(畳) <-> Square meter(㎡)"
    }

    override fun firstInputLabel(): String {
        return "Tatami counts(畳)"
    }

    override fun secondInputLabel(): String {
        return "㎡"
    }

    override fun defaultFirstInputValue(): String {
        return "8"
    }

    override fun defaultSecondInputValue(): String {
        return "12.96"
    }

    override fun firstInputAction(input: String): String? {
        val d = input.toDoubleOrNull() ?: return null
        return String.format("%.2f", d * FACTOR)
    }

    override fun secondInputAction(input: String): String? {
        val d = input.toDoubleOrNull() ?: return null
        return String.format("%.2f", d / FACTOR)
    }

    companion object {

        private const val FACTOR = 1.62f

    }

}