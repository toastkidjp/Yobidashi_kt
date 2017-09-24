package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okio.Okio
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * First collection of [Tab].
 *
 * @author toastkidjp
 */
class TabList private constructor() {

    @Transient private val tabs: MutableList<Tab>

    @Transient private val disposables = CompositeDisposable()

    private var index: Int = 0

    init {
        this.tabs = mutableListOf<Tab>()
    }

    internal fun currentTab(): Tab {
        return tabs.get(index)
    }

    internal fun setIndex(newIndex: Int) {
        index = newIndex
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
        val initJsonAdapterIfNeed = Completable.create { e ->
            initJsonAdapterIfNeed()
            e.onComplete()
        }
        val initTabJsonAdapterIfNeed = Completable.create { e ->
            initTabJsonAdapterIfNeed()
            e.onComplete()
        }

        disposables.add(Completable.ambArray(initJsonAdapterIfNeed, initTabJsonAdapterIfNeed)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            val json = jsonAdapter?.toJson(this)
                            val charset = charset("UTF-8")
                            Okio.buffer(Okio.sink(tabsFile))
                                    .write(json?.toByteArray(charset)).flush()
                            itemsDir?.list()
                                    ?.map { File(itemsDir, it) }
                                    ?.forEach { it.delete() }
                            tabs.forEach {
                                val source = tabJsonAdapter?.toJson(it)?.toByteArray(charset)
                                        ?: return@forEach
                                Okio.buffer(Okio.sink(File(itemsDir, "${it.id}.json")))
                                        .write(source)
                                        .flush()
                            }
                        },
                        { Timber.e(it) }
                ))
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
        File(itemsDir, tab.id + ".json").delete()
        tabs.remove(tab)
    }

    internal fun clear() {
        for (tab in tabs) {
            val lastScreenshot = File(tab.thumbnailPath)
            if (lastScreenshot.exists()) {
                lastScreenshot.delete()
            }
        }
        tabs.clear()
        index = 0
        tabsFile?.delete()
        itemsDir?.delete()
        save()
    }

    companion object {

        private val TABS_DIR = "tabs"

        private val TABS_ITEM_DIR = TABS_DIR + "/items"

        private var tabsFile: File? = null

        private var tabJsonAdapter: JsonAdapter<Tab>? = null

        private var jsonAdapter: JsonAdapter<TabList>? = null

        private var itemsDir: File? = null

        internal fun loadOrInit(context: Context): TabList {
            initTabsFile(context)
            if (tabsFile == null || !tabsFile!!.exists()) {
                return TabList()
            }

            try {
                initJsonAdapterIfNeed()
                initTabJsonAdapterIfNeed()

                val fromJson: TabList?
                        = jsonAdapter?.fromJson(Okio.buffer(Okio.source(tabsFile as File)))

                itemsDir?.list()
                        ?.map{ tabJsonAdapter?.fromJson(Okio.buffer(Okio.source(File(itemsDir, it))))}
                        ?.forEach { fromJson?.add(it as Tab) }
                if (fromJson?.size() as Int <= fromJson.index) {
                    fromJson.index = fromJson.size() - 1
                }
                return fromJson
            } catch (e: IOException) {
                Timber.e(e)
            }

            return TabList()
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

        private fun initTabJsonAdapterIfNeed() {
            if (tabJsonAdapter != null) {
                return
            }
            tabJsonAdapter = Moshi.Builder().build().adapter(Tab::class.java)
        }

        private fun initJsonAdapterIfNeed() {
            if (jsonAdapter != null) {
                return
            }
            jsonAdapter = Moshi.Builder().build().adapter(TabList::class.java)
        }
    }

    internal fun indexOf(tab: Tab): Int {
        return tabs.indexOf(tab)
    }

    fun dispose() {
        disposables.dispose()
    }

}
