/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.block

class SiteNameChecker {

    operator fun invoke(host: String?): Boolean {
        return host?.endsWith(".xyz") == true
                || host?.endsWith(".jp.net") == true
                || host == "rt.gsspat.jp"
                || host == "webnew.net"
                || host == "jp.img4.uk"
    }
}