/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.usecase

import android.net.Uri
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
class ImageSearchUrlGenerator {

    operator fun invoke(imageUrl: String): Uri =
        "https://www.bing.com/images/search?view=detailv2&iss=sbi&q=imgurl:${Uri.encode(imageUrl)}"
            .toUri()

}