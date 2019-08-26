/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.translation

import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateModelManager
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel
import timber.log.Timber

/**
 * Translation model loader.
 *
 * @author toastkidjp
 */
class TranslationModelLoader {

    /**
     * Language model manager.
     */
    private val modelManager = FirebaseTranslateModelManager.getInstance()

    /**
     * Download English and Japanese language models.
     */
    operator fun invoke() {
        modelManager
                .downloadRemoteModelIfNeeded(makeModel(FirebaseTranslateLanguage.EN))
                .addOnFailureListener { Timber.e(it) }
        modelManager
                .downloadRemoteModelIfNeeded(makeModel(FirebaseTranslateLanguage.JA))
                .addOnFailureListener { Timber.e(it) }
    }

    /**
     * Make model.
     *
     * @param language
     */
    private fun makeModel(language: Int) = FirebaseTranslateRemoteModel.Builder(language)
            .setDownloadConditions(makeCommonCondition())
            .build()

    /**
     * Make common condition, it contains requireWifi.
     */
    private fun makeCommonCondition() = FirebaseModelDownloadConditions.Builder()
            .requireWifi()
            .build()
}