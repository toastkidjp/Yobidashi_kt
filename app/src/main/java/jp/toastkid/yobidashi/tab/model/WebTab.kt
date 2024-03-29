package jp.toastkid.yobidashi.tab.model

import jp.toastkid.yobidashi.tab.History
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Model of web-browser tab.
 *
 * @author toastkidjp
 */
@Serializable
@SerialName("web")
internal class WebTab: Tab {

    @Required
    private val histories: MutableList<History> = mutableListOf()

    @Required
    private val id: String = UUID.randomUUID().toString()

    @Synchronized override fun back(): String {
        return histories[0].url()
    }

    @Synchronized override fun forward(): String {
        return histories[0].url()
    }

    fun addHistory(history: History?) {
        if (history == null || "about:blank".equals(history.url())) {
            return
        }

        if (histories.isEmpty()) histories.add(history) else histories.set(0, history)
    }

    val latest: History
        get() {
            if (histories.isEmpty()) {
                return History.EMPTY
            }
            return histories[0]
        }

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) {
        latest.scrolled = scrollY
    }

    override fun getScrolled(): Int = latest.scrolled

    override fun getUrl(): String = latest.url()

    override fun title(): String = latest.title()

    companion object {

        fun make(title: String, url: String): WebTab = WebTab().also {
            it.addHistory(History.make(title, url))
        }

    }

}
