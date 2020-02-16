/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.graphics.Matrix

/**
 * @author toastkidjp
 */
class RotateMatrixFactory {

    operator fun invoke(degrees: Float, width: Float, height: Float) =
            Matrix().also { it.setRotate(degrees, width / 2f, height / 2f) }

}