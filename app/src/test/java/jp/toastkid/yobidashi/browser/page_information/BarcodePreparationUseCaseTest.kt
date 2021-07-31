/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.page_information

import android.view.View
import android.widget.ImageView
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class BarcodePreparationUseCaseTest {

    private lateinit var barcodePreparationUseCase: BarcodePreparationUseCase

    @MockK
    private lateinit var contentView: View

    @MockK
    private lateinit var imageView: ImageView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { contentView.findViewById<ImageView>(any()) }.returns(imageView)
        coEvery { imageView.setImageBitmap(any()) }.just(Runs)
        coEvery { imageView.setVisibility(any()) }.just(Runs)
        coEvery { imageView.getContext() }.returns(mockk())
        coEvery { imageView.setOnClickListener(any()) }.returns(mockk())

        mockkConstructor(BarcodeEncoder::class)
        coEvery { anyConstructed<BarcodeEncoder>().encodeBitmap(any(), any(), any(), any()) }
            .returns(mockk())

        barcodePreparationUseCase = BarcodePreparationUseCase(Dispatchers.Unconfined, Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        barcodePreparationUseCase.invoke(contentView, "https://www.yahoo.co.jp")

        coVerify(atLeast = 1) { contentView.findViewById<ImageView>(any()) }
        coVerify(exactly = 1) { imageView.setImageBitmap(any()) }
        coVerify(exactly = 1) { imageView.setVisibility(any()) }
        coVerify(exactly = 1) { imageView.getContext() }
        coVerify(exactly = 1) { imageView.setOnClickListener(any()) }
        coVerify(exactly = 1) {
            anyConstructed<BarcodeEncoder>().encodeBitmap(any(), any(), any(), any())
        }
    }

}