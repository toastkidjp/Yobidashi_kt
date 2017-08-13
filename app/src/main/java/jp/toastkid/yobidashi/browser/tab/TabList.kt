package jp.toastkid.yobidashi.browser.tab

import android.content.Context

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import java.io.File
import java.io.IOException
import java.util.ArrayList

import okio.Okio

/**
 * First collection of [Tab].

 * @author toastkidjp
 */
class TabList private constructor() : Iterable<Tab> {

    private val tabs: MutableList<Tab>

    private var index: Int = 0

    init {
        this.tabs = ArrayList<Tab>()
    }

    internal fun currentTab(): Tab {
        return tabs[index]
    }

    internal fun setIndex(newIndex: Int) {
        index = newIndex
    }

    fun size(): Int {
        return tabs.size
    }

    operator fun get(position: Int): Tab {
        return tabs[position]
    }

    override fun iterator(): Iterator<Tab> {
        return tabs.iterator()
    }

    /**
     * Save current state to file.
     */
    internal fun save() {
        try {
            initJsonAdapterIfNeed()
            val json = jsonAdapter!!.toJson(this)
            Okio.buffer(Okio.sink(tabsFile!!)).write(json.toByteArray(charset("UTF-8"))).flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    internal val isEmpty: Boolean
        get() = tabs.isEmpty()

    internal fun add(newTab: Tab) {
        tabs.add(newTab)
    }

    private fun remove(index: Int) {
        tabs.removeAt(index)
    }

    internal fun closeTab(index: Int) {
        if (index <= this.index) {
            this.index--
        }
        remove(index)
        save()
    }

    internal fun clear() {
        tabs.clear()
        index = -1
        tabsFile!!.delete()
    }

    companion object {

        private val TABS_DIR = "tabs"

        private var tabsFile: File? = null

        private var jsonAdapter: JsonAdapter<TabList>? = null

        internal fun loadOrInit(context: Context): TabList {
            initTabsFile(context)
            if (tabsFile == null || !tabsFile!!.exists()) {
                return TabList()
            }

            try {
                initJsonAdapterIfNeed()
                return jsonAdapter!!.fromJson(Okio.buffer(Okio.source(tabsFile!!)))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return TabList()
        }

        private fun initTabsFile(context: Context) {
            val storeDir = File(context.filesDir, TABS_DIR)
            if (!storeDir.exists()) {
                storeDir.mkdirs()
            }
            tabsFile = File(storeDir, "tabs.json")
        }

        private fun initJsonAdapterIfNeed() {
            if (jsonAdapter != null) {
                return
            }
            jsonAdapter = Moshi.Builder().build().adapter(TabList::class.java)
        }
    }
}
