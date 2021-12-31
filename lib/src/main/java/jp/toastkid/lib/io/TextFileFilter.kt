/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.io

import java.io.File
import java.io.FileFilter

class TextFileFilter : FileFilter {

    override fun accept(pathname: File?): Boolean {
        val name = pathname?.name ?: return false
        return name.endsWith(".txt") || name.endsWith(".md")
    }

}