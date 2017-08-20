package jp.toastkid.yobidashi.browser

import android.support.v7.app.AlertDialog
import android.view.View
import io.reactivex.functions.Consumer
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Browser's user agent.

 * @author toastkidjp
 */
enum class UserAgent private constructor(private val title: String, private val text: String) {
    DEFAULT("Default", ""),
    ANDROID("Android", "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 5X Build/N4F26I) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.91 Mobile Safari/537.36"),
    IPHONE("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_2 like Mac OS X) AppleWebKit/602.3.12 (KHTML, like Gecko) Version/10.0 Mobile/14C92 Safari/602.1\n"),
    IPAD("iPad", "Mozilla/5.0 (iPad; CPU OS 10_2 like Mac OS X) AppleWebKit/602.3.12 (KHTML, like Gecko) Version/10.0 Mobile/14C92 Safari/602.1"),
    PC("PC", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");

    fun title(): String {
        return title
    }

    fun text(): String {
        return text
    }

    companion object {

        fun titles(): Array<String> {
            val titles = ArrayList<String>(values().size)
            for (i in 0..values().size - 1) {
                titles.add(values()[i].title)
            }
            return titles.toArray(arrayOf<String>())
        }

        fun findCurrentIndex(name: String): Int {
            for (i in 0..values().size - 1) {
                if (values()[i].name == name) {
                    return i
                }
            }
            return 0
        }

        /**
         * Show selection dialog.
         * @param snackbarParent
         * *
         * @param callback
         */
        fun showSelectionDialog(
                snackbarParent: View,
                callback: (UserAgent) -> Unit
        ) {
            val context = snackbarParent.context
            val preferenceApplier = PreferenceApplier(context)
            AlertDialog.Builder(context)
                    .setTitle(R.string.title_user_agent)
                    .setSingleChoiceItems(
                            UserAgent.titles(),
                            findCurrentIndex(preferenceApplier.userAgent())
                    ) { d, i ->
                        val userAgent = UserAgent.values()[i]
                        preferenceApplier.setUserAgent(userAgent.name)
                        callback(userAgent)
                        Toaster.snackShort(
                                snackbarParent,
                                context.getString(R.string.format_result_user_agent, userAgent.title()),
                                preferenceApplier.colorPair()
                        )
                    }
                    .setCancelable(true)
                    .setNegativeButton(R.string.close) { d, i -> d.cancel() }
                    .show()
        }
    }
}
