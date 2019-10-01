/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.translation

import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions

/**
 * @author toastkidjp
 */
class J2ETranslator : Translator {

    private val translator =
            FirebaseNaturalLanguage.getInstance()
                    .getTranslator(
                            FirebaseTranslatorOptions.Builder()
                                    .setSourceLanguage(FirebaseTranslateLanguage.JA)
                                    .setTargetLanguage(FirebaseTranslateLanguage.EN)
                                    .build()
                    )

    override operator fun invoke(
            text: String?,
            onSuccess: (String?) -> Unit,
            onError: (Exception) -> Unit
            ) {
        if (text.isNullOrBlank()) {
            return
        }
        translator.translate(text)
                .addOnSuccessListener { onSuccess(it) }
                .addOnFailureListener { onError(it) }
    }
}