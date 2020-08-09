/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.page_information

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author toastkidjp
 */
class BarcodePreparationUseCase {

    private val imageCache = ImageCache()

    operator fun invoke(contentView: View, url: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            val imageView = contentView.findViewById<ImageView>(R.id.barcode)
            val bitmap = withContext(Dispatchers.IO) {
                BarcodeEncoder()
                        .encodeBitmap(url, BarcodeFormat.QR_CODE, BARCODE_SIZE, BARCODE_SIZE)
            }
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
            setShareAction(bitmap, contentView.findViewById(R.id.share))
        }
    }

    private fun setShareAction(bitmap: Bitmap, shareView: View?) {
        val context = shareView?.context ?: return

        shareView.setOnClickListener {
            val uri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    imageCache.saveBitmap(context.cacheDir, bitmap).absoluteFile
            )
            try {
                context.startActivity(IntentFactory.shareImage(uri))
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
            }
        }
    }

    companion object {

        /**
         * Barcode size.
         */
        private const val BARCODE_SIZE = 400
    }
}