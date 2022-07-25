package jp.toastkid.yobidashi.tab

import jp.toastkid.yobidashi.tab.model.WebTab
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * [WebTab]'s test case.
 *
 * @author toastkidjp
 */
class WebTabTest {

    @Test
    @Throws(IOException::class)
    fun test() {
        val tab = makeTestTab()

        val tabJsonAdapter = makeTabJsonAdapter()
        val json = check_toJson(tab, tabJsonAdapter)

        check_fromJson(tabJsonAdapter, json)
    }

    @Throws(IOException::class)
    private fun check_fromJson(tabJsonAdapter: Json, json: String) {
        val fromJson = tabJsonAdapter.decodeFromString<WebTab>(json)
        assertEquals("Title", fromJson?.latest?.title())
        assertEquals("URL", fromJson?.latest?.url())
    }

    private fun check_toJson(tab: WebTab, tabJsonAdapter: Json): String {
        val json = tabJsonAdapter.encodeToString(tab)
        assertTrue(json.contains("\"title\":\"Title\""))
        assertTrue(json.contains("\"url\":\"URL\""))
        return json
    }

    private fun makeTabJsonAdapter(): Json {
        return Json { ignoreUnknownKeys = true }
    }

    private fun makeTestTab(): WebTab {
        val tab = WebTab()
        tab.addHistory(History.make("Title", "URL"))
        return tab
    }
}
