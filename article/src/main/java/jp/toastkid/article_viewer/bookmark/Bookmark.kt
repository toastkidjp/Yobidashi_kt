/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.bookmark

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author toastkidjp
 */
@Entity
data class Bookmark(@PrimaryKey(autoGenerate = false) var id: Int)