package jp.toastkid.about.view

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutThisAppUiKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutThisAppUi() {
        composeTestRule.setContent {
            AboutThisAppUi("test")
        }
    }

}
