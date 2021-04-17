package jp.toastkid.yobidashi.main.launch

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import androidx.fragment.app.Fragment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.barcode.BarcodeReaderFragment
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.launcher.LauncherFragment
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.settings.SettingFragment
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LauncherIntentUseCaseTest {

    @MockK
    private lateinit var randomWikipediaUseCase: RandomWikipediaUseCase

    @MockK
    private lateinit var openNewWebTab: (Uri) -> Unit

    @MockK
    private lateinit var openEditorTab: (Uri) -> Unit

    @MockK
    private lateinit var search: (String, String) -> Unit

    @MockK
    private lateinit var searchCategoryFinder: () -> String

    @MockK
    private lateinit var replaceFragment: (Class<out Fragment>) -> Unit

    @MockK
    private lateinit var elseCaseUseCase: ElseCaseUseCase

    @MockK
    private lateinit var intent: Intent

    private lateinit var launcherIntentUseCase: LauncherIntentUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { randomWikipediaUseCase.invoke() }.answers { Unit }
        every { openEditorTab.invoke(any()) }.answers { Unit }
        every { openNewWebTab.invoke(any()) }.answers { Unit }
        every { replaceFragment.invoke(any()) }.answers { Unit }
        every { elseCaseUseCase.invoke() }.answers { Unit }

        launcherIntentUseCase = LauncherIntentUseCase(
                randomWikipediaUseCase,
                openNewWebTab,
                openEditorTab,
                search,
                searchCategoryFinder,
                replaceFragment,
                elseCaseUseCase
        )
    }

    @Test
    fun testRandomWikipedia() {
        every { intent.getBooleanExtra(any(), false) }.answers { true }
        every { intent.getAction() }.answers { "" }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 1) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 0) { intent.getAction() }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testViewDataIsNull() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_VIEW }
        every { intent.getData() }.answers { null }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { intent.getData() }
        verify(exactly = 0) { openEditorTab.invoke(any()) }
        verify(exactly = 0) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testViewDataIsContentScheme() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_VIEW }
        val data = mockk<Uri>()
        every { data.scheme }.answers { "content" }
        every { intent.getData() }.answers { data }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { intent.getData() }
        verify(exactly = 1) { openEditorTab.invoke(any()) }
        verify(exactly = 0) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testViewDataIsOtherScheme() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_VIEW }
        val data = mockk<Uri>()
        every { data.scheme }.answers { "https" }
        every { intent.getData() }.answers { data }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { intent.getData() }
        verify(exactly = 0) { openEditorTab.invoke(any()) }
        verify(exactly = 1) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testSendSearchCase() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_SEND }

        val extras = spyk(Bundle())
        every { extras.getCharSequence(Intent.EXTRA_TEXT) }.answers { "test" }
        every { intent.getExtras() }.answers { extras }
        every { search.invoke(any(), any()) }.answers { Unit }
        every { searchCategoryFinder.invoke() }.answers { "test" }
        every { openNewWebTab.invoke(any()) }.answers { Unit }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 0) { intent.getData() }
        verify(exactly = 1) { extras.getCharSequence(Intent.EXTRA_TEXT) }
        verify(exactly = 1) { intent.getExtras() }
        verify(exactly = 1) { search.invoke(any(), any()) }
        verify(exactly = 1) { searchCategoryFinder.invoke() }
        verify(exactly = 0) { openEditorTab.invoke(any()) }
        verify(exactly = 0) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testSendBrowseCase() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_SEND }

        val extras = spyk(Bundle())
        every { extras.getCharSequence(Intent.EXTRA_TEXT) }.answers { "https://www.yahoo.co.jp" }
        every { intent.getExtras() }.answers { extras }
        mockkStatic(URLUtil::class)
        every { URLUtil.isHttpsUrl(any()) }.answers { true }
        every { search.invoke(any(), any()) }.answers { Unit }
        every { searchCategoryFinder.invoke() }.answers { "test" }
        every { openNewWebTab.invoke(any()) }.answers { Unit }
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.answers { mockk() }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 0) { intent.getData() }
        verify(exactly = 1) { extras.getCharSequence(Intent.EXTRA_TEXT) }
        verify(exactly = 1) { intent.getExtras() }
        verify(exactly = 0) { search.invoke(any(), any()) }
        verify(exactly = 0) { searchCategoryFinder.invoke() }
        verify(exactly = 0) { openEditorTab.invoke(any()) }
        verify(exactly = 1) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testWebSearchWithFavoriteSearch() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_WEB_SEARCH }
        every { intent.hasExtra(LauncherIntentUseCase.EXTRA_KEY_CATEGORY) }.answers { true }
        every { intent.getStringExtra(LauncherIntentUseCase.EXTRA_KEY_CATEGORY) }.answers { "test" }
        every { intent.getStringExtra(SearchManager.QUERY) }.answers { "test" }
        every { search.invoke(any(), any()) }.answers { Unit }
        every { searchCategoryFinder.invoke() }.answers { "test" }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 0) { intent.getData() }
        verify(exactly = 1) { search.invoke(any(), any()) }
        verify(exactly = 0) { searchCategoryFinder.invoke() }
        verify(exactly = 0) { openEditorTab.invoke(any()) }
        verify(exactly = 0) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testWebSearch() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { Intent.ACTION_WEB_SEARCH }
        every { intent.hasExtra(LauncherIntentUseCase.EXTRA_KEY_CATEGORY) }.answers { false }
        every { intent.getStringExtra(LauncherIntentUseCase.EXTRA_KEY_CATEGORY) }.answers { "test" }
        every { intent.getStringExtra(SearchManager.QUERY) }.answers { "test" }
        every { search.invoke(any(), any()) }.answers { Unit }
        every { searchCategoryFinder.invoke() }.answers { "test" }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 0) { intent.getData() }
        verify(exactly = 1) { search.invoke(any(), any()) }
        verify(exactly = 1) { searchCategoryFinder.invoke() }
        verify(exactly = 0) { openEditorTab.invoke(any()) }
        verify(exactly = 0) { openNewWebTab.invoke(any()) }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testBookmark() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { BOOKMARK }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { replaceFragment.invoke(BookmarkFragment::class.java) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testBarcodeReader() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { BARCODE_READER }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { replaceFragment.invoke(BarcodeReaderFragment::class.java) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testSearch() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { SEARCH }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { replaceFragment.invoke(SearchFragment::class.java) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testSetting() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { SETTING }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 1) { replaceFragment.invoke(SettingFragment::class.java) }
        verify(exactly = 0) { elseCaseUseCase.invoke() }
    }

    @Test
    fun testElse() {
        every { intent.getBooleanExtra(any(), false) }.answers { false }
        every { intent.getAction() }.answers { "else" }

        launcherIntentUseCase.invoke(intent)

        verify(exactly = 0) { randomWikipediaUseCase.invoke() }
        verify(exactly = 1) { intent.getBooleanExtra(any(), false) }
        verify(exactly = 1) { intent.getAction() }
        verify(exactly = 0) { replaceFragment.invoke(any()) }
        verify(exactly = 1) { elseCaseUseCase.invoke() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}