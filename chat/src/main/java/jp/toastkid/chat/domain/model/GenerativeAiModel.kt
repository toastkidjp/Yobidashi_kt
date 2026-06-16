/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.chat.domain.model

enum class GenerativeAiModel(
    private val label: String,
    private val urlParameter: String,
    private val shortLabel: String,
    private val versionPath: String,
    private val webGrounding: Boolean = false,
    private val image: Boolean = false
) {

    GEMINI_3_5_FLASH(
        "Gemini 3.5 Flash",
        "gemini-3.5-flash",
        "3.5",
        "v1beta"
    ),
    GEMINI_3_0_FLASH(
        "Gemini 3 Flash",
        "gemini-3-flash-preview",
        "3.0",
        "v1beta"
    ),
    GEMINI_2_5_FLASH(
        "Gemini 2.5 Flash",
        "gemini-2.5-flash",
        "2.5",
        "v1beta",
        webGrounding = true,
    ),
    GEMINI_2_5_FLASH_LITE(
        "Gemini 2.5 Flash Lite",
        "gemini-2.5-flash-lite",
        "2.5 lite",
        "v1beta",
        webGrounding = true,
    ),
    GEMINI_2_5_FLASH_WITHOUT_WEB_GROUNDING(
        "Gemini 2.5 Flash(Web Grounding なし)",
        "gemini-2.5-flash",
        "2.5",
        "v1beta",
    ),
    GEMINI_2_5_FLASH_LITE_WITHOUT_WEB_GROUNDING(
        "Gemini 2.5 Flash Lite(Web Grounding なし)",
        "gemini-2.5-flash-lite",
        "2.5 lite",
        "v1beta",
    ),
    ;

    fun label(): String = label

    fun url(): String {
        return "https://generativelanguage.googleapis.com/${versionPath}/models/${urlParameter}" +
                ":streamGenerateContent?alt=sse&key="
    }

    fun webGrounding() = webGrounding

    fun image() = image

    fun version() = shortLabel

}
