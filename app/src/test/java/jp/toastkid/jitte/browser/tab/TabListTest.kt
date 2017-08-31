package jp.toastkid.jitte.browser.tab

import com.squareup.moshi.Moshi
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
        tabList.add(Tab())
        tabList.add(makeTab())

        val adapter = Moshi.Builder().build().adapter(TabList::class.java)
        val json = adapter.toJson(tabList)
        assertEquals(0, JSONObject(json).getInt("index"))

        val fromJson = adapter.fromJson(json)
        assertEquals(0, fromJson?.size())
    }

    private fun makeTab(): Tab {
        val tab = Tab()
        tab.addHistory(History.make("title", "url"))
        tab.addHistory(History.make("title2", "url2"))
        tab.thumbnailPath = "thumbnailPath"
        return tab
    }
}