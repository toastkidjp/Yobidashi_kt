/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.intent

import android.content.Intent

class CreateDocumentIntentFactory {

    /**
     * Make create document intent on Storage Access Framework.
     *
     * @param type mime type
     * @param fileName File name
     * @return [Intent]
     */
    operator fun invoke(type: String, fileName: String): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = type
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        return intent
    }

}