package jp.toastkid.yobidashi.browser.user_agent

/**
 * Browser's user agent.
 *
 * @author toastkidjp
 */
enum class UserAgent constructor(private val title: String, private val text: String) {
    DEFAULT("Default", ""),
    ANDROID("Android", "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 5X Build/N4F26I) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.91 Mobile Safari/537.36"),
    IPHONE("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_2 like Mac OS X) AppleWebKit/602.3.12 (KHTML, like Gecko) Version/10.0 Mobile/14C92 Safari/602.1\n"),
    IPAD("iPad", "Mozilla/5.0 (iPad; CPU OS 10_2 like Mac OS X) AppleWebKit/602.3.12 (KHTML, like Gecko) Version/10.0 Mobile/14C92 Safari/602.1"),
    PC("PC", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");

    fun title(): String = title

    fun text(): String = text

    companion object {

        fun titles(): Array<String> {
            val titles = ArrayList<String>(values().size)
            for (i in 0..values().size - 1) {
                titles.add(values()[i].title)
            }
            return titles.toArray(arrayOf<String>())
        }

        fun findCurrentIndex(name: String): Int {
            for (i in 0 until values().size) {
                if (values()[i].name == name) {
                    return i
                }
            }
            return 0
        }
    }
}
