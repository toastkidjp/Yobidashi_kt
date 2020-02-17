/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.random

import java.util.*

/**
 * Generator for Wikipedia domain.
 *
 * @author toastkidjp
 */
class UrlDecider {

    /**
     * Generate Wikipedia domain.
     *
     * @return Wikipedia domain
     */
    operator fun invoke() = "https://${Locale.getDefault().language}.wikipedia.org/"
}