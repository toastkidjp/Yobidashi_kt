package jp.toastkid.yobidashi.tab

import jp.toastkid.yobidashi.tab.model.WebTab
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.IOException

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class TabListTest {

    @Test
    @Throws(IOException::class, JSONException::class)
    fun test() {
        val tabList = TabList.loadOrInit(RuntimeEnvironment.application)
        tabList.add(WebTab())
        tabList.add(makeTab())

        val adapter = Json { ignoreUnknownKeys = true }
        val json = adapter.encodeToString(tabList)
        assertEquals(0, JSONObject(json).getInt("index"))

        val fromJson = adapter.decodeFromString<TabList>(json)
        assertEquals(0, fromJson?.size())
    }

    private fun makeTab(): WebTab {
        val tab = WebTab()
        tab.addHistory(History.make("title", "url"))
        tab.addHistory(History.make("title2", "url2"))
        return tab
    }
}