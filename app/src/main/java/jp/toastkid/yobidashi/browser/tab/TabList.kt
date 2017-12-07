package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.reactivex.disposables.CompositeDisposable
import okio.Okio
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * First collection of [Tab].
 *
 * @author toastkidjp
 */
class TabList private constructor() {

    @Transient private val tabs: MutableList<Tab> = mutableListOf<Tab>()

    @Transient private val disposables = CompositeDisposable()

    private var index: Int = 0

    internal fun currentTab(): Tab {
        return tabs.get(index)
    }

    internal fun setIndex(newIndex: Int) {
        index = if (newIndex < 0 || tabs.size < newIndex) 0 else newIndex
    }

    fun getIndex(): Int {
        return index
    }

    fun size(): Int {
        return tabs.size
    }

    internal fun get(position: Int): Tab {
        if (position < 0 || tabs.size <= position) {
            return tabs[0]
        }
        return tabs[position]
    }

    internal fun set(index: Int, currentTab: Tab) {
        if (index < 0 || tabs.size < index) {
            tabs.set(0, currentTab)
            return
        }
        tabs.set(index, currentTab)
    }

    /**
     * Save current state to file.
     */
    internal fun save() {
        val json = jsonAdapter.toJson(this)
        tabsFile?.let { Okio.buffer(Okio.sink(it)).write(json.toByteArray(charset)).flush() }
        savingLock.withLock {
            itemsDir?.let {
                it.deleteRecursively()
                it.mkdirs()
            }
            tabs.forEach { tab ->
                val source: ByteArray? = when {
                    tab is WebTab    -> webTabJsonAdapter.toJson(tab)?.toByteArray(charset)
                    tab is EditorTab -> editorTabJsonAdapter.toJson(tab)?.toByteArray(charset)
                    else             -> ByteArray(0)
                }
                source?.let {
                    Okio.buffer(Okio.sink(File(itemsDir, "${tab.id()}.json")))
                            .write(source)
                            .flush()
                }
            }
        }
    }

    internal val isEmpty: Boolean
        get() = tabs.isEmpty()

    internal fun add(newTab: Tab) {
        tabs.add(newTab)
    }

    internal fun closeTab(index: Int) {
        if (index <= this.index) {
            this.index--
        }
        val tab: Tab = tabs.get(index)
        remove(tab)
    }

    private fun remove(tab: Tab) {
        File(itemsDir, tab.id() + ".json").delete()
        tabs.remove(tab)
    }

    internal fun clear() {
        for (tab in tabs) {
            tab.deleteLastThumbnail()
        }
        tabs.clear()
        index = 0
        tabsFile?.delete()
        itemsDir?.delete()
        save()
    }

    companion object {

        private const val TABS_DIR = "tabs"

        private const val TABS_ITEM_DIR = TABS_DIR + "/items"

        private val savingLock = ReentrantLock()

        private var tabsFile: File? = null

        private val charset = charset("UTF-8")

        private val jsonAdapter: JsonAdapter<TabList> by lazy {
            Moshi.Builder().build().adapter(TabList::class.java)
        }

        private val webTabJsonAdapter: JsonAdapter<WebTab> by lazy {
            Moshi.Builder().build().adapter(WebTab::class.java)
        }

        private val editorTabJsonAdapter: JsonAdapter<EditorTab> by lazy {
            Moshi.Builder().build().adapter(EditorTab::class.java)
        }

        private var itemsDir: File? = null

        internal fun loadOrInit(context: Context): TabList {
            initTabsFile(context)
            if (tabsFile == null || !tabsFile!!.exists()) {
                return TabList()
            }

            try {
                val fromJson: TabList?
                        = jsonAdapter.fromJson(Okio.buffer(Okio.source(tabsFile as File)))

                loadTabsFromDir()
                        ?.forEach { it?.let { fromJson?.add(it) } }
                if (fromJson?.size() as Int <= fromJson.index) {
                    fromJson.index = fromJson.size() - 1
                }
                return fromJson
            } catch (e: IOException) {
                Timber.e(e)
            }

            return TabList()
        }

        internal fun loadTabsFromDir(): List<Tab?>? {
            return itemsDir?.list()
                    ?.map {
                        val json: String = Okio.buffer(Okio.source(File(itemsDir, it))).readUtf8()
                        if (json.contains("editorTab")) {
                            editorTabJsonAdapter.fromJson(json)
                        } else {
                            webTabJsonAdapter.fromJson(json)
                        }
                    }
        }

        private fun initTabsFile(context: Context) {
            val storeDir = File(context.filesDir, TABS_DIR)
            if (!storeDir.exists()) {
                storeDir.mkdirs()
            }
            tabsFile = File(storeDir, "tabs.json")

            itemsDir = File(context.filesDir, TABS_ITEM_DIR)
            if (itemsDir != null && !(itemsDir as File).exists()) {
                itemsDir?.mkdirs()
            }
        }
    }

    internal fun indexOf(tab: Tab): Int {
        return tabs.indexOf(tab)
    }

    fun dispose() {
        disposables.clear()
    }

    override fun toString(): String = tabs.toString()

    /**
     * Load background tab from file if needs.
     */
    fun loadBackgroundTabsFromDirIfNeed() {
        BackgroundTabQueue.iterate {
            tabs.add(WebTab.makeBackground(it.first, it.second.toString()))
        }
        save()
    }

}
