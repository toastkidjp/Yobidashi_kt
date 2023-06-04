/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.licence.model

import jp.toastkid.licence.model.text.Apache2
import jp.toastkid.licence.model.text.Bsd2
import jp.toastkid.licence.model.text.Mit

class LicensesLoader {

    private val targetNewItem = "- artifact:"

    private val licenseTexts = setOf(
        Apache2(),
        Mit(),
        Bsd2()
    )

    operator fun invoke(yamlLines: List<String>): List<License> {
        val licenses = mutableListOf<License>()
        var title = ""
        var copyright = ""
        var url = ""
        var text = ""
        yamlLines.forEach { line ->
            if (line.startsWith(targetNewItem) && title.isNotEmpty()) {
                licenses.add(License(title, copyright, url, text))
            }
            when {
                line.contains("name:") -> title = line.split("name:")[1].trim()
                line.contains("copyrightHolder:") -> copyright = line.split("copyrightHolder:")[1]
                line.contains("url:") -> url = line.split("url:")[1].trim()
                line.contains("licenseUrl:") -> {
                    val licenseUrl = line.split("licenseUrl:")[1].trim()
                    text = licenseTexts.firstOrNull { it.url() == licenseUrl }?.text() ?: ""
                }
            }
        }
        licenses.add(License(title, copyright, url, text))
        return licenses
    }

}