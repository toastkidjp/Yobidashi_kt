package jp.toastkid.jitte.browser.tab

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * [Tab]'s test case.
 *
 * @author toastkidjp
 */
class TabTest {

    @Test
    @Throws(IOException::class)
    fun test() {
        val tab = makeTestTab()

        val tabJsonAdapter = makeTabJsonAdapter()
        val json = check_toJson(tab, tabJsonAdapter)

        check_fromJson(tabJsonAdapter, json)
    }

    @Throws(IOException::class)
    private fun check_fromJson(tabJsonAdapter: JsonAdapter<Tab>, json: String) {
        val fromJson = tabJsonAdapter.fromJson(json)
        assertEquals("Google", fromJson!!.lastTitle)
        assertEquals("file://~~", fromJson.thumbnailPath)
        assertEquals("Title", fromJson.latest.title())
        assertEquals("URL", fromJson.latest.url())
    }

    private fun check_toJson(tab: Tab, tabJsonAdapter: JsonAdapter<Tab>): String {
        val json = tabJsonAdapter.toJson(tab)
        assertTrue(json.contains("\"histories\":[{\"title\":\"Title\",\"url\":\"URL\"}]"))
        assertTrue(json.contains("\"index\":0"))
        assertTrue(json.contains("\"lastTitle\":\"Google\""))
        assertTrue(json.contains("\"thumbnailPath\":\"file://~~\""))
        return json
    }

    private fun makeTabJsonAdapter(): JsonAdapter<Tab> {
        val moshi = Moshi.Builder().build()
        return moshi.adapter(Tab::class.java)
    }

    private fun makeTestTab(): Tab {
        val tab = Tab()
        tab.thumbnailPath = "file://~~"
        tab.lastTitle = "Google"
        tab.addHistory(History.make("Title", "URL"))
        return tab
    }
}
