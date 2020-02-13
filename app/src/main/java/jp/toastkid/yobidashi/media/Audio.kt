/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

/**
 * @author toastkidjp
 */
data class Audio(
        val id: Long?,
        val path: String?,
        val title: String?,
        val artist: String?,
        val albumId: Long?,
        val album: String?,
        val date: Long?
)